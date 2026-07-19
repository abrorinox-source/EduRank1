package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.*

// ==========================================
// 1. LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    viewModel: EduViewModel,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    val groups by viewModel.groups.collectAsState()

    val loginState by viewModel.loginState.collectAsState()
    val signUpState by viewModel.signUpState.collectAsState()

    val activeState = if (isSignUpMode) signUpState else loginState
    val isLoading = activeState is UiState.Loading

    // Handle success routing
    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) {
            onLoginSuccess((loginState as UiState.Success<User>).data)
            viewModel.clearLoginState()
        }
    }

    LaunchedEffect(signUpState) {
        if (signUpState is UiState.Success) {
            onLoginSuccess((signUpState as UiState.Success<User>).data)
            viewModel.clearSignUpState()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 420.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo & Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Indigo, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "EduRank Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "EduRank",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = if (isSignUpMode) "Yangi hisob yaratish" else "Education Center Performance & Rankings",
                    fontSize = 12.sp,
                    color = SlateLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Error Message
                if (activeState is UiState.Error) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RoseRedLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (activeState as UiState.Error).message,
                            color = RoseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Full Name Input (Only on Sign Up)
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("To'liq ism-sharifingiz") },
                        placeholder = { Text("Ali Valiyev") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo,
                            focusedLabelColor = Indigo
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("fullname_input")
                    )
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("user@edurank.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedLabelColor = Indigo
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("email_input")
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedLabelColor = Indigo
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("password_input")
                )

                // Role Selection Selector (Only on Sign Up)
                if (isSignUpMode) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Rolingizni tanlang:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserRole.values().forEach { role ->
                                val isSelected = selectedRole == role
                                val roleText = when (role) {
                                    UserRole.STUDENT -> "Student"
                                    UserRole.TEACHER -> "Teacher"
                                    UserRole.ADMIN -> "Admin"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Indigo else BackgroundGray)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Indigo else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roleText,
                                        color = if (isSelected) Color.White else SlateDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Group Selection (Only on Sign Up as Student)
                if (isSignUpMode && selectedRole == UserRole.STUDENT) {
                    OutlinedTextField(
                        value = selectedGroup?.name ?: "Guruh tanlanmagan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Guruhni tanlang") },
                        trailingIcon = {
                            IconButton(onClick = { showGroupDialog = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Guruhlar ro'yxati")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGroupDialog = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo,
                            focusedLabelColor = Indigo,
                            disabledTextColor = SlateDark,
                            disabledBorderColor = Color(0xFFCBD5E1),
                            disabledLabelColor = SlateLight
                        ),
                        enabled = false
                    )

                    if (showGroupDialog) {
                        AlertDialog(
                            onDismissRequest = { showGroupDialog = false },
                            title = { Text("Guruhni tanlang", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (groups.isEmpty()) {
                                        Text("Hozircha guruhlar mavjud emas", color = SlateLight, fontSize = 14.sp)
                                    } else {
                                        groups.forEach { group ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (selectedGroup?.id == group.id) IndigoLight else BackgroundGray)
                                                    .clickable {
                                                        selectedGroup = group
                                                        showGroupDialog = false
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = group.name,
                                                    color = if (selectedGroup?.id == group.id) Indigo else SlateDark,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showGroupDialog = false }) {
                                    Text("Yopish", color = Indigo)
                                }
                            }
                        )
                    }
                }



                // Submit Button
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            viewModel.signUp(email, password, fullName, selectedRole, selectedGroup?.id)
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button"),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isSignUpMode) "Ro'yxatdan o'tish" else "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                // Mode Toggle Link
                Text(
                    text = if (isSignUpMode) "Sizda allaqachon hisob bormi? Kirish" else "Yangi hisob yaratish (Sign Up)",
                    color = Indigo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable {
                            isSignUpMode = !isSignUpMode
                            viewModel.clearLoginState()
                            viewModel.clearSignUpState()
                        }
                        .padding(vertical = 4.dp)
                )


            }
        }
    }
}


// ==========================================
// 1B. PENDING APPROVAL SCREEN
// ==========================================
@Composable
fun PendingApprovalScreen(
    viewModel: EduViewModel,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(IndigoLight, RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Pending approval",
                        tint = Indigo,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Hisobingiz tasdiqlanishi kutilmoqda",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Siz muvaffaqiyatli ro'yxatdan o'tdingiz! O'qituvchi profilingizni tasdiqlaganidan so'ng tizimga to'liq kirishingiz mumkin bo'ladi.",
                    fontSize = 14.sp,
                    color = SlateLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Tizimdan chiqish", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}


// ==========================================
// 2. STUDENT DASHBOARD SCREEN
// ==========================================
@Composable
fun StudentDashboardScreen(
    viewModel: EduViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val personalTx by viewModel.personalTransactions.collectAsState()
    val groupRankings by viewModel.groupRankings.collectAsState()
    val userRank by viewModel.currentUserRank.collectAsState()

    val student = currentUser ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        // Stats Hero Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Indigo)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Greeting and Role Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Welcome back,",
                            fontSize = 13.sp,
                            color = IndigoLight,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = student.fullName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Student", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Balance & Rank display row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Points Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Balance", fontSize = 11.sp, color = IndigoLight)
                            Text(
                                text = String.format("%,d", student.points),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("pts", fontSize = 11.sp, color = IndigoLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Rank Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Group Rank", fontSize = 11.sp, color = IndigoLight)
                            Text(
                                text = userRank?.let { "#$it" } ?: "N/A",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "out of ${groupRankings.size} active",
                                fontSize = 11.sp,
                                color = IndigoLight
                            )
                        }
                    }
                }
            }
        }

        // Navigation Quick Shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToTransfer,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("student_nav_transfer")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Transfer Points", fontSize = 12.sp, color = Color.White)
            }

            Button(
                onClick = onNavigateToRanking,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("student_nav_ranking")
            ) {
                Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Indigo)
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Rankings", fontSize = 12.sp, color = Indigo)
            }
        }

        // Personal History Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Points History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            TextButton(onClick = onNavigateToHistory) {
                Text("See All", fontSize = 12.sp, color = Indigo)
            }
        }

        // Recent transactions list (capped at 4)
        if (personalTx.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No transaction history yet", fontSize = 13.sp, color = SlateLight)
            }
        } else {
            val studentNameMap = groupRankings.associate { it.id to it.fullName } + (student.id to student.fullName)
            personalTx.take(4).forEach { tx ->
                val senderName = studentNameMap[tx.fromUserId] ?: "Unknown Student"
                val receiverName = studentNameMap[tx.toUserId] ?: "Unknown Student"
                val isReceived = tx.toUserId == student.id

                TransactionRow(
                    tx = tx,
                    senderName = senderName,
                    receiverName = receiverName,
                    isReceived = isReceived
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}


// ==========================================
// 3. STUDENT PROFILE SCREEN
// ==========================================
@Composable
fun StudentProfileScreen(
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val student = currentUser ?: return
    val groups by viewModel.groups.collectAsState()
    val studentGroup = groups.find { it.id == student.groupId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large profile avatar bubble
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(IndigoLight, CircleShape)
                .border(2.dp, Indigo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initials = student.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
            Text(initials, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Indigo)
        }

        Text(student.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SlateDark)
        StatusBadge(student.status)

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Student Credentials", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                
                ProfileItem(label = "Email Address", value = student.email, icon = Icons.Default.Email)
                ProfileItem(label = "Phone Number", value = student.phone.ifEmpty { "Not specified" }, icon = Icons.Default.Phone)
                ProfileItem(label = "Assigned Group", value = studentGroup?.name ?: "No assigned group", icon = Icons.Default.Group)
                ProfileItem(label = "Class Schedule", value = studentGroup?.schedule ?: "Schedule unavailable", icon = Icons.Default.Schedule)
                ProfileItem(label = "Total Points Balance", value = String.format("%,d pts", student.points), icon = Icons.Default.MonetizationOn)
            }
        }
    }
}

@Composable
private fun ProfileItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SlateLight, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontSize = 10.sp, color = SlateLight, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 13.sp, color = SlateDark, fontWeight = FontWeight.Medium)
        }
    }
}


// ==========================================
// 4. GROUP RANKINGS SCREEN (STUDENT PREVIEW)
// ==========================================
@Composable
fun GroupRankingScreen(
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    val groupRankings by viewModel.groupRankings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val groups by viewModel.groups.collectAsState()
    
    val student = currentUser ?: return
    val group = groups.find { it.id == student.groupId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Group Header banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Indigo)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = group?.name ?: "Group Rankings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Weekly/Monthly point accumulation dashboard",
                    fontSize = 11.sp,
                    color = IndigoLight
                )
            }
        }

        if (groupRankings.isEmpty()) {
            EmptyState(message = "No active students in your group ranking.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(groupRankings.mapIndexed { i, student -> Pair(i + 1, student) }) { (rank, peer) ->
                    RankCard(
                        rank = rank,
                        studentName = peer.fullName,
                        points = peer.points,
                        groupName = group?.name ?: "Group",
                        isCurrentUser = peer.id == student.id
                    )
                }
            }
        }
    }
}


// ==========================================
// 5. TRANSFER POINTS SCREEN
// ==========================================
@Composable
fun TransferPointsScreen(
    viewModel: EduViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val groupPeers by viewModel.groupPeers.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    val student = currentUser ?: return
    
    // Filters peers to avoid transferring points to oneself or inactive users
    val eligiblePeers = groupPeers.filter { it.id != student.id && it.status == UserStatus.ACTIVE }

    var selectedPeerId by remember { mutableStateOf("") }
    var pointsText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Dropdown state
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedPeer = eligiblePeers.find { it.id == selectedPeerId }

    LaunchedEffect(actionState) {
        if (actionState is UiState.Success) {
            onSuccess()
            viewModel.clearActionState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Your Available Balance", fontSize = 11.sp, color = SlateLight, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%,d pts", student.points),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(IndigoLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Indigo)
                }
            }
        }

        Text("Point Transfer Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateDark)

        if (eligiblePeers.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "There are no other active students in your group to transfer points to.",
                    color = SlateLight,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Dropdown Selector
            Box {
                OutlinedTextField(
                    value = selectedPeer?.fullName ?: "Select classmate...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Transfer Recipient") },
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedLabelColor = Indigo
                    ),
                    modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true }.testTag("peer_selector")
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    eligiblePeers.forEach { peer ->
                        DropdownMenuItem(
                            text = { Text(peer.fullName) },
                            onClick = {
                                selectedPeerId = peer.id
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("peer_item_${peer.id}")
                        )
                    }
                }
            }

            // Points Field
            OutlinedTextField(
                value = pointsText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        pointsText = it
                        errorMsg = null
                    }
                },
                label = { Text("Points amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo,
                    focusedLabelColor = Indigo
                ),
                modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input")
            )

            // Note/Reason
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Reason") },
                singleLine = true,
                placeholder = { Text("Study guide helper, presentation assistant...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo,
                    focusedLabelColor = Indigo
                ),
                modifier = Modifier.fillMaxWidth().testTag("transfer_note_input")
            )

            // Action States or Validation Error UI
            errorMsg?.let {
                Text(it, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (actionState is UiState.Error) {
                Text((actionState as UiState.Error).message, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Transfer Trigger Button
            Button(
                onClick = {
                    val pts = pointsText.toIntOrNull()
                    if (selectedPeerId.isEmpty()) {
                        errorMsg = "Please select a transfer recipient"
                    } else if (pts == null || pts <= 0) {
                        errorMsg = "Please enter a valid positive points amount"
                    } else if (pts > student.points) {
                        errorMsg = "You cannot transfer more points than your balance (${student.points} pts)"
                    } else if (noteText.trim().isBlank()) {
                        errorMsg = "Please enter a brief reason for the transfer"
                    } else {
                        errorMsg = null
                        showConfirmDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("transfer_points_button"),
                enabled = actionState !is UiState.Loading
            ) {
                if (actionState is UiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Transfer Points", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
            }
        }

        // Confirmation Dialog Overlay
        if (showConfirmDialog && selectedPeer != null) {
            ConfirmDialog(
                title = "Confirm Points Transfer",
                message = "Are you sure you want to transfer ${pointsText} points to ${selectedPeer.fullName}? This operation cannot be undone.",
                confirmText = "Transfer Now",
                onConfirm = {
                    showConfirmDialog = false
                    val pts = pointsText.toInt()
                    viewModel.transferPoints(selectedPeerId, pts, noteText)
                },
                onDismiss = { showConfirmDialog = false }
            )
        }
    }
}


// ==========================================
// 6. TRANSACTION HISTORY SCREEN
// ==========================================
@Composable
fun TransactionHistoryScreen(
    viewModel: EduViewModel,
    modifier: Modifier = Modifier
) {
    val personalTx by viewModel.personalTransactions.collectAsState()
    val groupPeers by viewModel.groupRankings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val student = currentUser ?: return
    val studentNameMap = groupPeers.associate { it.id to it.fullName } + (student.id to student.fullName)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        if (personalTx.isEmpty()) {
            EmptyState(message = "You have no point adjustment or transfer history yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(personalTx) { tx ->
                    val senderName = studentNameMap[tx.fromUserId] ?: "Unknown Student"
                    val receiverName = studentNameMap[tx.toUserId] ?: "Unknown Student"
                    val isReceived = tx.toUserId == student.id

                    TransactionRow(
                        tx = tx,
                        senderName = senderName,
                        receiverName = receiverName,
                        isReceived = isReceived
                    )
                }
            }
        }
    }
}


// ==========================================
// 7. TEACHER DASHBOARD HUB (GROUPS SCREEN)
// ==========================================
@Composable
fun GroupListScreen(
    viewModel: EduViewModel,
    onNavigateToTable: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val groups by viewModel.authorizedGroups.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var newGroupSchedule by remember { mutableStateOf("") }
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState) {
        if (actionState is UiState.Success && showCreateGroupDialog) {
            showCreateGroupDialog = false
            newGroupName = ""
            newGroupSchedule = ""
            viewModel.clearActionState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Assigned Groups",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )
            Button(
                onClick = { showCreateGroupDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("create_group_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guruh yaratish", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (showCreateGroupDialog) {
            AlertDialog(
                onDismissRequest = { showCreateGroupDialog = false },
                title = { Text("Yangi guruh yaratish", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (actionState is UiState.Error) {
                            Text(
                                text = (actionState as UiState.Error).message,
                                color = RoseRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedTextField(
                            value = newGroupName,
                            onValueChange = { newGroupName = it },
                            label = { Text("Guruh nomi") },
                            placeholder = { Text("Web Development, IELTS 7.0, vb.") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newGroupSchedule,
                            onValueChange = { newGroupSchedule = it },
                            label = { Text("Dars kunlari va vaqti") },
                            placeholder = { Text("Du-Chor-Ju 14:00 - 16:00") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.createGroup(newGroupName, newGroupSchedule) },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                        enabled = actionState !is UiState.Loading
                    ) {
                        if (actionState is UiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Yaratish")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateGroupDialog = false }) {
                        Text("Bekor qilish", color = SlateLight)
                    }
                }
            )
        }

        if (groups.isEmpty()) {
            EmptyState(message = "You do not have any groups assigned to manage.")
        } else {
            groups.forEach { group ->
                val count = allStudents.count { it.groupId == group.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTable(group.id) }
                        .testTag("group_card_${group.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(IndigoLight, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = Indigo)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = "Schedule: ${group.schedule}",
                                fontSize = 11.sp,
                                color = SlateLight
                            )
                        }

                        // Student Count Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count Std",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 8. STUDENT TABLE SPREADSHEET SCREEN
// ==========================================
@Composable
fun StudentTableScreen(
    viewModel: EduViewModel,
    onNavigateToAddStudent: (String) -> Unit,
    onNavigateToEditStudent: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredStudents by viewModel.filteredStudents.collectAsState()
    val currentGroupId by viewModel.selectedGroupId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val showInactive by viewModel.showInactiveStudents.collectAsState()

    val currentGroup = groups.find { it.id == currentGroupId }
    val groupNameMap = groups.associate { it.id to it.name }

    // Dialog state for point adjustment
    var adjustDialogStudent by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Quick statistics, filtering and searching Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search student name, phone, email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateLight) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedLabelColor = Indigo
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("student_search_input")
                )

                // Sort & Filters buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sorting dropdown indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val nextSort = when (sortBy) {
                                StudentSortType.POINTS -> StudentSortType.NAME
                                StudentSortType.NAME -> StudentSortType.POINTS
                                else -> StudentSortType.POINTS
                            }
                            viewModel.setSortBy(nextSort)
                        }.testTag("sort_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (sortBy == StudentSortType.POINTS) Icons.Default.SortByAlpha else Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Indigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sort: ${sortBy.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Indigo
                        )
                    }

                    // Show inactive toggle check
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.setShowInactiveStudents(!showInactive) }
                            .testTag("inactive_toggle")
                    ) {
                        Checkbox(
                            checked = showInactive,
                            onCheckedChange = { viewModel.setShowInactiveStudents(it) },
                            colors = CheckboxDefaults.colors(checkedColor = Indigo)
                        )
                        Text("Show Inactive", fontSize = 11.sp, color = SlateDark)
                    }
                }
            }
        }

        // Table List
        if (filteredStudents.isEmpty()) {
            EmptyState(message = "No matching students found in this group.")
        } else {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        SpreadsheetTable(
                            students = filteredStudents,
                            groupNameMap = groupNameMap,
                            onStudentClick = { onNavigateToEditStudent(it) },
                            onQuickAdd = { adjustDialogStudent = it },
                            onQuickSubtract = { adjustDialogStudent = it }
                        )
                    }
                }

                // Add Floating action button
                LargeFloatingActionButton(
                    onClick = { currentGroupId?.let { onNavigateToAddStudent(it) } },
                    containerColor = Indigo,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(56.dp)
                        .testTag("add_student_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Student", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        // Point Adjustment Overlay Dialog
        adjustDialogStudent?.let { student ->
            PointAdjustmentDialog(
                studentName = student.fullName,
                onConfirm = { amount, type, note ->
                    viewModel.adjustPoints(student.id, amount, type, note)
                    adjustDialogStudent = null
                },
                onDismiss = { adjustDialogStudent = null }
            )
        }
    }
}


// ==========================================
// 9. ADD STUDENT SCREEN
// ==========================================
@Composable
fun AddStudentScreen(
    viewModel: EduViewModel,
    groupId: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pointsText by remember { mutableStateOf("0") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState) {
        if (actionState is UiState.Success) {
            onSuccess()
            viewModel.clearActionState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Register New Student", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateDark)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("add_student_name")
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("add_student_email")
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("add_student_phone")
        )

        OutlinedTextField(
            value = pointsText,
            onValueChange = { if (it.all { char -> char.isDigit() }) pointsText = it },
            label = { Text("Initial Point Balance") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("add_student_points")
        )

        errorMsg?.let {
            Text(it, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (actionState is UiState.Error) {
            Text((actionState as UiState.Error).message, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank()) {
                    errorMsg = "Full Name and Email are required fields"
                } else {
                    errorMsg = null
                    val pts = pointsText.toIntOrNull() ?: 0
                    viewModel.addStudent(name, email, phone, groupId, pts)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Indigo),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_student_submit"),
            enabled = actionState !is UiState.Loading
        ) {
            if (actionState is UiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Register Student", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}


// ==========================================
// 10. EDIT STUDENT SCREEN
// ==========================================
@Composable
fun EditStudentScreen(
    viewModel: EduViewModel,
    student: User,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(student.fullName) }
    var email by remember { mutableStateOf(student.email) }
    var phone by remember { mutableStateOf(student.phone) }
    var isActive by remember { mutableStateOf(student.status == UserStatus.ACTIVE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState) {
        if (actionState is UiState.Success) {
            onSuccess()
            viewModel.clearActionState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Edit Student Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateDark)

        if (student.status == UserStatus.PENDING) {
            Card(
                colors = CardDefaults.cardColors(containerColor = IndigoLight.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Indigo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tasdiqlash kutilmoqda (Pending Approval)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo
                    )
                    Text(
                        text = "Ushbu talaba guruhga qo'shilish uchun ro'yxatdan o'tgan va tasdiqlanishingizni kutmoqda.",
                        fontSize = 11.sp,
                        color = SlateDark,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.approveStudent(student) },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("approve_student_button")
                    ) {
                        Text("Tasdiqlash (Approve)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("edit_student_name")
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("edit_student_email")
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Indigo, focusedLabelColor = Indigo),
            modifier = Modifier.fillMaxWidth().testTag("edit_student_phone")
        )

        // Status Row Switcher
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Student Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    Text("Inactive students are excluded from rankings", fontSize = 11.sp, color = SlateLight)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Indigo, checkedTrackColor = IndigoLight),
                    modifier = Modifier.testTag("edit_student_status_switch")
                )
            }
        }

        errorMsg?.let {
            Text(it, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (actionState is UiState.Error) {
            Text((actionState as UiState.Error).message, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank()) {
                    errorMsg = "Full Name and Email are required fields"
                } else {
                    errorMsg = null
                    val updated = student.copy(
                        fullName = name,
                        email = email,
                        phone = phone,
                        status = if (isActive) UserStatus.ACTIVE else UserStatus.INACTIVE
                    )
                    viewModel.updateStudentInfo(updated)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Indigo),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("edit_student_submit"),
            enabled = actionState !is UiState.Loading
        ) {
            if (actionState is UiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
