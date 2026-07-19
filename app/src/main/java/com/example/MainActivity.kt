package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.data.repository.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.BackgroundGray
import com.example.ui.theme.Indigo
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.viewmodel.EduViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely try to initialize Firebase components
        val firebaseAuth = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("EduRank", "Firebase Auth not available, running in Mock Mode. Error: ${e.message}")
            null
        }

        val firebaseFirestore = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("EduRank", "Firebase Firestore not available, running in Mock Mode. Error: ${e.message}")
            null
        }

        // Shared Mock database for offline/fallback mode
        val mockDatabase = MockDatabase()

        // Repositories
        val authRepository = AuthRepositoryImpl(firebaseFirestore, firebaseAuth, mockDatabase)
        val eduRepository = EduRepositoryImpl(firebaseFirestore, authRepository, mockDatabase)

        setContent {
            MyApplicationTheme {
                val viewModel: EduViewModel = viewModel(
                    factory = EduViewModelFactory(authRepository, eduRepository)
                )

                EduRankAppShell(viewModel)
            }
        }
    }
}

// Custom Viewmodel Factory for EduViewModel
class EduViewModelFactory(
    private val authRepo: AuthRepository,
    private val eduRepo: EduRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EduViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EduViewModel(authRepo, eduRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Navigation Routes
object Routes {
    const val LOGIN = "login"
    const val PENDING_APPROVAL = "pending_approval"
    
    // Student Core
    const val STUDENT_DASHBOARD = "student_dashboard"
    const val STUDENT_RANKINGS = "student_rankings"
    const val STUDENT_TRANSFER = "student_transfer"
    const val STUDENT_HISTORY = "student_history"
    const val STUDENT_PROFILE = "student_profile"

    // Teacher/Admin Core
    const val TEACHER_GROUPS = "teacher_groups"
    const val TEACHER_HISTORY = "teacher_history"
    const val TEACHER_STUDENTS = "teacher_students/{groupId}"
    const val TEACHER_ADD_STUDENT = "teacher_add_student/{groupId}"
    const val TEACHER_EDIT_STUDENT = "teacher_edit_student"

    fun teacherStudents(groupId: String) = "teacher_students/$groupId"
    fun teacherAddStudent(groupId: String) = "teacher_add_student/$groupId"
}

@Composable
fun EduRankAppShell(viewModel: EduViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle initial state and auth shifts
    LaunchedEffect(currentUser) {
        val user = currentUser
        val activeRoute = navController.currentBackStackEntry?.destination?.route ?: Routes.LOGIN
        if (user == null) {
            if (activeRoute != Routes.LOGIN) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else if (user.status == UserStatus.PENDING) {
            if (activeRoute != Routes.PENDING_APPROVAL) {
                navController.navigate(Routes.PENDING_APPROVAL) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // User is authenticated and active.
            // Only redirect to the dashboard if they are currently on LOGIN or PENDING_APPROVAL.
            if (activeRoute == Routes.LOGIN || activeRoute == Routes.PENDING_APPROVAL) {
                val startDestination = when (user.role) {
                    UserRole.STUDENT -> Routes.STUDENT_DASHBOARD
                    UserRole.TEACHER, UserRole.ADMIN -> Routes.TEACHER_GROUPS
                }
                navController.navigate(startDestination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Determine the type of screens and navigation bars to show
    val isLoginScreen = currentRoute == Routes.LOGIN || currentUser == null || currentRoute == Routes.PENDING_APPROVAL
    val isStudentRole = currentUser?.role == UserRole.STUDENT && currentUser?.status != UserStatus.PENDING
    val isTeacherRole = (currentUser?.role == UserRole.TEACHER || currentUser?.role == UserRole.ADMIN) && currentUser?.status != UserStatus.PENDING

    // Sub-screen checking (for displaying a Back arrow)
    val rootDestinations = listOf(
        Routes.LOGIN,
        Routes.PENDING_APPROVAL,
        Routes.STUDENT_DASHBOARD,
        Routes.STUDENT_RANKINGS,
        Routes.STUDENT_TRANSFER,
        Routes.STUDENT_PROFILE,
        Routes.TEACHER_GROUPS,
        Routes.TEACHER_HISTORY
    )
    val showBackButton = currentRoute != null && currentRoute !in rootDestinations

    Scaffold(
        topBar = {
            if (!isLoginScreen && currentUser != null) {
                val subtitle = when {
                    viewModel.isUsingMockMode() -> "Offline Mock Mode • ${currentUser!!.role.name}"
                    currentUser!!.role == UserRole.STUDENT -> "Student Panel"
                    currentUser!!.role == UserRole.TEACHER -> "Teacher Panel"
                    currentUser!!.role == UserRole.ADMIN -> "Admin Panel"
                    else -> "Panel"
                }

                val initials = currentUser!!.fullName.split(" ")
                    .mapNotNull { it.firstOrNull() }
                    .take(2)
                    .joinToString("")
                    .uppercase()

                // Custom Top App Bar with back action support
                Column {
                    CommonTopBar(
                        title = "EduRank",
                        subtitle = subtitle,
                        userInitials = initials,
                        onLogoutClick = { viewModel.logout() }
                    )
                    
                    // Header Back Nav indicator if we are inside a nested screen (Add student, edit student, spreadsheet, etc.)
                    if (showBackButton) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .border(width = (0.5).dp, color = Color(0xFFE2E8F0))
                                .clickable { navController.popBackStack() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go Back",
                                tint = Indigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Back to previous screen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isLoginScreen && currentUser != null) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(width = (0.5).dp, color = Color(0xFFE2E8F0))
                ) {
                    if (isStudentRole) {
                        // Student Bottom Navigation Bars
                        NavigationBarItem(
                            selected = currentRoute == Routes.STUDENT_DASHBOARD,
                            onClick = { navController.navigate(Routes.STUDENT_DASHBOARD) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo,
                                selectedTextColor = Indigo,
                                indicatorColor = IndigoLight,
                                unselectedIconColor = SlateLight,
                                unselectedTextColor = SlateLight
                            ),
                            modifier = Modifier.testTag("nav_student_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.STUDENT_RANKINGS,
                            onClick = { navController.navigate(Routes.STUDENT_RANKINGS) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Rankings") },
                            label = { Text("Rankings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo,
                                selectedTextColor = Indigo,
                                indicatorColor = IndigoLight,
                                unselectedIconColor = SlateLight,
                                unselectedTextColor = SlateLight
                            ),
                            modifier = Modifier.testTag("nav_student_rankings")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.STUDENT_TRANSFER,
                            onClick = { navController.navigate(Routes.STUDENT_TRANSFER) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Send, contentDescription = "Transfer") },
                            label = { Text("Transfer", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo,
                                selectedTextColor = Indigo,
                                indicatorColor = IndigoLight,
                                unselectedIconColor = SlateLight,
                                unselectedTextColor = SlateLight
                            ),
                            modifier = Modifier.testTag("nav_student_transfer")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.STUDENT_PROFILE,
                            onClick = { navController.navigate(Routes.STUDENT_PROFILE) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo,
                                selectedTextColor = Indigo,
                                indicatorColor = IndigoLight,
                                unselectedIconColor = SlateLight,
                                unselectedTextColor = SlateLight
                            ),
                            modifier = Modifier.testTag("nav_student_profile")
                        )
                    } else if (isTeacherRole) {
                        // Teacher/Admin Bottom Navigation Bars
                        NavigationBarItem(
                            selected = currentRoute == Routes.TEACHER_GROUPS || currentRoute?.startsWith("teacher_students") == true,
                            onClick = { navController.navigate(Routes.TEACHER_GROUPS) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Groups, contentDescription = "Groups") },
                            label = { Text("Groups", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo,
                                selectedTextColor = Indigo,
                                indicatorColor = IndigoLight,
                                unselectedIconColor = SlateLight,
                                unselectedTextColor = SlateLight
                            ),
                            modifier = Modifier.testTag("nav_teacher_groups")
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.TEACHER_HISTORY,
                            onClick = { navController.navigate(Routes.TEACHER_HISTORY) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History Logs", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo,
                                selectedTextColor = Indigo,
                                indicatorColor = IndigoLight,
                                unselectedIconColor = SlateLight,
                                unselectedTextColor = SlateLight
                            ),
                            modifier = Modifier.testTag("nav_teacher_history")
                        )
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Login
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { user ->
                        // The Shell's LaunchedEffect(currentUser) handles reactive navigation
                    }
                )
            }

            // 1B. Pending Approval
            composable(Routes.PENDING_APPROVAL) {
                PendingApprovalScreen(
                    viewModel = viewModel,
                    onLogoutClick = {
                        viewModel.logout()
                    }
                )
            }

            // ==========================================
            // STUDENT PORTAL SCREENS
            // ==========================================
            composable(Routes.STUDENT_DASHBOARD) {
                StudentDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { navController.navigate(Routes.STUDENT_PROFILE) },
                    onNavigateToRanking = { navController.navigate(Routes.STUDENT_RANKINGS) },
                    onNavigateToTransfer = { navController.navigate(Routes.STUDENT_TRANSFER) },
                    onNavigateToHistory = { navController.navigate(Routes.STUDENT_HISTORY) }
                )
            }

            composable(Routes.STUDENT_RANKINGS) {
                GroupRankingScreen(viewModel = viewModel)
            }

            composable(Routes.STUDENT_TRANSFER) {
                TransferPointsScreen(
                    viewModel = viewModel,
                    onSuccess = {
                        navController.navigate(Routes.STUDENT_DASHBOARD) {
                            popUpTo(Routes.STUDENT_DASHBOARD) { inclusive = false }
                        }
                    }
                )
            }

            composable(Routes.STUDENT_PROFILE) {
                StudentProfileScreen(viewModel = viewModel)
            }

            composable(Routes.STUDENT_HISTORY) {
                TransactionHistoryScreen(viewModel = viewModel)
            }


            // ==========================================
            // TEACHER PORTAL SCREENS
            // ==========================================
            composable(Routes.TEACHER_GROUPS) {
                GroupListScreen(
                    viewModel = viewModel,
                    onNavigateToTable = { groupId ->
                        viewModel.setSelectedGroupId(groupId)
                        navController.navigate(Routes.teacherStudents(groupId))
                    }
                )
            }

            composable(Routes.TEACHER_HISTORY) {
                val transactions by viewModel.allTransactions.collectAsState()
                val students by viewModel.allStudents.collectAsState()
                val studentNameMap = students.associate { it.id to it.fullName }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Indigo)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Universal Point History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Full transparency audit log of adjustments & peer-to-peer transfers", fontSize = 11.sp, color = IndigoLight)
                        }
                    }

                    if (transactions.isEmpty()) {
                        EmptyState(message = "No manual point changes or peer transfers found.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(transactions) { tx ->
                                val senderName = studentNameMap[tx.fromUserId] ?: "System/Student"
                                val receiverName = studentNameMap[tx.toUserId] ?: "Student"
                                TransactionRow(
                                    tx = tx,
                                    senderName = senderName,
                                    receiverName = receiverName,
                                    isReceived = true // Teacher view presents all transactions as universal
                                )
                            }
                        }
                    }
                }
            }

            // Spreadsheet Student management Screen
            composable(Routes.TEACHER_STUDENTS) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                LaunchedEffect(groupId) {
                    viewModel.setSelectedGroupId(groupId)
                }

                StudentTableScreen(
                    viewModel = viewModel,
                    onNavigateToAddStudent = { gId ->
                        navController.navigate(Routes.teacherAddStudent(gId))
                    },
                    onNavigateToEditStudent = { student ->
                        viewModel.selectedStudentForEdit = student
                        navController.navigate(Routes.TEACHER_EDIT_STUDENT)
                    }
                )
            }

            // Register Student screen
            composable(Routes.TEACHER_ADD_STUDENT) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                AddStudentScreen(
                    viewModel = viewModel,
                    groupId = groupId,
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            // Edit Student screen
            composable(Routes.TEACHER_EDIT_STUDENT) {
                val editingStudent = viewModel.selectedStudentForEdit
                if (editingStudent == null) {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                } else {
                    EditStudentScreen(
                        viewModel = viewModel,
                        student = editingStudent,
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
