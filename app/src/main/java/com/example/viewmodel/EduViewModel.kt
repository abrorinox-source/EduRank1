package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AuthRepository
import com.example.data.repository.EduRepository
import java.util.UUID
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class StudentSortType {
    NAME, POINTS, RANKING
}

class EduViewModel(
    private val authRepository: AuthRepository,
    private val eduRepository: EduRepository
) : ViewModel() {

    // --- State for Edit Student Navigation ---
    var selectedStudentForEdit: User? = null


    // --- Authentication State ---
    val currentUser: StateFlow<User?> = authRepository.getCurrentUser()

    private val _loginState = MutableStateFlow<UiState<User>?>(null)
    val loginState: StateFlow<UiState<User>?> = _loginState.asStateFlow()

    fun isUsingMockMode(): Boolean = authRepository.isUsingMockMode()

    fun setForceMockMode(enabled: Boolean) {
        authRepository.setForceMockMode(enabled)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = UiState.Error("Email and Password cannot be blank")
            return
        }
        _loginState.value = UiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .catch { e ->
                    _loginState.value = UiState.Error(e.localizedMessage ?: "Unknown Login Error")
                }
                .collect { result ->
                    result.onSuccess { user ->
                        _loginState.value = UiState.Success(user)
                    }.onFailure { e ->
                        _loginState.value = UiState.Error(e.localizedMessage ?: "Invalid email or password")
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect {
                _loginState.value = null
            }
        }
    }

    fun clearLoginState() {
        _loginState.value = null
    }

    private val _signUpState = MutableStateFlow<UiState<User>?>(null)
    val signUpState: StateFlow<UiState<User>?> = _signUpState.asStateFlow()

    fun signUp(email: String, password: String, fullName: String, role: UserRole, groupId: String? = null) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank() || (role == UserRole.STUDENT && groupId.isNullOrBlank())) {
            _signUpState.value = UiState.Error("Iltimos, barcha maydonlarni to'ldiring")
            return
        }
        _signUpState.value = UiState.Loading
        viewModelScope.launch {
            authRepository.signUp(email, password, fullName, role, groupId)
                .catch { e ->
                    _signUpState.value = UiState.Error(e.localizedMessage ?: "Ro'yxatdan o'tishda xatolik yuz berdi")
                }
                .collect { result ->
                    result.onSuccess { user ->
                        _signUpState.value = UiState.Success(user)
                    }.onFailure { e ->
                        _signUpState.value = UiState.Error(e.localizedMessage ?: "Ro'yxatdan o'tish muvaffaqiyatsiz tugadi")
                    }
                }
        }
    }

    fun clearSignUpState() {
        _signUpState.value = null
    }

    // --- Shared Core Data ---
    val groups: StateFlow<List<Group>> = eduRepository.getGroups()
        .retry { kotlinx.coroutines.delay(2000); true }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allStudents: StateFlow<List<User>> = currentUser.flatMapLatest { user ->
        if (user != null) eduRepository.getStudents().retry { kotlinx.coroutines.delay(2000); true } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allTransactions: StateFlow<List<PointTransaction>> = currentUser.flatMapLatest { user ->
        if (user != null) eduRepository.getTransactions().retry { kotlinx.coroutines.delay(2000); true } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Filter & Navigation States ---
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showInactiveStudents = MutableStateFlow(false)
    val showInactiveStudents: StateFlow<Boolean> = _showInactiveStudents.asStateFlow()

    private val _sortBy = MutableStateFlow(StudentSortType.POINTS)
    val sortBy: StateFlow<StudentSortType> = _sortBy.asStateFlow()

    fun setSelectedGroupId(groupId: String?) {
        _selectedGroupId.value = groupId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowInactiveStudents(show: Boolean) {
        _showInactiveStudents.value = show
    }

    fun setSortBy(sortType: StudentSortType) {
        _sortBy.value = sortType
    }

    // --- Student Computed States ---
    // User's group peers (for student dashboards)
    val groupPeers: StateFlow<List<User>> = combine(allStudents, currentUser) { students, user ->
        val gId = user?.groupId
        if (gId != null) {
            students.filter { it.groupId == gId }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Calculated ranking list for current student's group
    val groupRankings: StateFlow<List<User>> = groupPeers.map { peers ->
        peers.filter { it.status == UserStatus.ACTIVE }
            .sortedWith(
                compareByDescending<User> { it.points }
                    .thenBy { it.fullName.lowercase() }
            )
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // User's rank inside their group
    val currentUserRank: StateFlow<Int?> = combine(groupRankings, currentUser) { rankings, user ->
        if (user == null) return@combine null
        val index = rankings.indexOfFirst { it.id == user.id }
        if (index != -1) index + 1 else null
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // User's own personal point transactions list
    val personalTransactions: StateFlow<List<PointTransaction>> = combine(allTransactions, currentUser) { txs, user ->
        if (user == null) return@combine emptyList()
        txs.filter { it.fromUserId == user.id || it.toUserId == user.id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // --- Teacher/Admin Computed States ---
    // Group filter: Teachers can only manage groups assigned to them (except admin)
    val authorizedGroups: StateFlow<List<Group>> = combine(groups, currentUser) { allGroups, user ->
        if (user == null) return@combine emptyList()
        if (user.role == UserRole.ADMIN) {
            allGroups
        } else {
            allGroups.filter { it.teacherId == user.id }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // List of students filtered by group, search query, status, sorted, and ranked
    val filteredStudents: StateFlow<List<User>> = combine(
        allStudents,
        selectedGroupId,
        searchQuery,
        showInactiveStudents,
        sortBy
    ) { students, groupId, query, showInactive, sortType ->
        var list = students

        // Filter by Group
        if (groupId != null) {
            list = list.filter { it.groupId == groupId }
        }

        // Filter by Search Query (name/email/phone)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.fullName.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.phone.contains(q)
            }
        }

        // Filter by Status
        if (!showInactive) {
            list = list.filter { it.status == UserStatus.ACTIVE || it.status == UserStatus.PENDING }
        }

        // Sort
        when (sortType) {
            StudentSortType.NAME -> list.sortedBy { it.fullName.lowercase() }
            StudentSortType.POINTS -> list.sortedWith(
                compareByDescending<User> { it.points }
                    .thenBy { it.fullName.lowercase() }
            )
            StudentSortType.RANKING -> list.sortedWith(
                compareByDescending<User> { it.points }
                    .thenBy { it.fullName.lowercase() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // --- Mutators: Transfers and Adjustments ---
    private val _actionState = MutableStateFlow<UiState<Unit>?>(null)
    val actionState: StateFlow<UiState<Unit>?> = _actionState.asStateFlow()

    fun clearActionState() {
        _actionState.value = null
    }

    fun transferPoints(toStudentId: String, amount: Int, note: String) {
        val sender = currentUser.value ?: return
        if (sender.points < amount) {
            _actionState.value = UiState.Error("You cannot transfer more points than you currently have (${sender.points} pts)")
            return
        }
        if (amount <= 0) {
            _actionState.value = UiState.Error("Points to transfer must be greater than zero")
            return
        }

        _actionState.value = UiState.Loading
        viewModelScope.launch {
            eduRepository.performTransfer(
                fromUserId = sender.id,
                toUserId = toStudentId,
                amount = amount,
                note = note,
                createdBy = sender.id
            ).collect { result ->
                result.onSuccess {
                    _actionState.value = UiState.Success(Unit)
                }.onFailure { e ->
                    _actionState.value = UiState.Error(e.localizedMessage ?: "Transfer failed")
                }
            }
        }
    }

    fun adjustPoints(targetStudentId: String, amount: Int, type: TransactionType, note: String) {
        val operator = currentUser.value ?: return
        if (amount <= 0) {
            _actionState.value = UiState.Error("Points value must be greater than zero")
            return
        }

        _actionState.value = UiState.Loading
        viewModelScope.launch {
            eduRepository.performAdjustment(
                targetUserId = targetStudentId,
                amount = amount,
                type = type,
                note = note,
                createdBy = operator.id
            ).collect { result ->
                result.onSuccess {
                    _actionState.value = UiState.Success(Unit)
                }.onFailure { e ->
                    _actionState.value = UiState.Error(e.localizedMessage ?: "Adjustment failed")
                }
            }
        }
    }

    // --- Student Management (Add / Edit) ---
    fun addStudent(fullName: String, email: String, phone: String, groupId: String, initialPoints: Int) {
        val operator = currentUser.value ?: return
        if (fullName.isBlank() || email.isBlank()) {
            _actionState.value = UiState.Error("Name and Email are required")
            return
        }

        _actionState.value = UiState.Loading
        viewModelScope.launch {
            val newStudent = User(
                id = "student_" + UUID.randomUUID().toString().take(6),
                fullName = fullName,
                email = email,
                phone = phone,
                role = UserRole.STUDENT,
                groupId = groupId,
                points = initialPoints,
                status = UserStatus.ACTIVE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            eduRepository.createStudent(newStudent).collect { result ->
                result.onSuccess {
                    // Create an initial point change record if initialPoints > 0
                    if (initialPoints > 0) {
                        eduRepository.performAdjustment(
                            targetUserId = newStudent.id,
                            amount = initialPoints,
                            type = TransactionType.MANUAL_ADD,
                            note = "Initial entry points",
                            createdBy = operator.id
                        ).collect { adjResult ->
                            adjResult.onSuccess {
                                _actionState.value = UiState.Success(Unit)
                            }.onFailure { e ->
                                _actionState.value = UiState.Success(Unit) // Student created, but entry adjustment failed
                            }
                        }
                    } else {
                        _actionState.value = UiState.Success(Unit)
                    }
                }.onFailure { e ->
                    _actionState.value = UiState.Error(e.localizedMessage ?: "Failed to add student")
                }
            }
        }
    }

    fun updateStudentInfo(student: User) {
        if (student.fullName.isBlank() || student.email.isBlank()) {
            _actionState.value = UiState.Error("Name and Email are required")
            return
        }

        _actionState.value = UiState.Loading
        viewModelScope.launch {
            eduRepository.updateStudent(student.copy(updatedAt = System.currentTimeMillis())).collect { result ->
                result.onSuccess {
                    _actionState.value = UiState.Success(Unit)
                }.onFailure { e ->
                    _actionState.value = UiState.Error(e.localizedMessage ?: "Failed to update student")
                }
            }
        }
    }

    fun createGroup(name: String, schedule: String) {
        val operator = currentUser.value ?: return
        if (name.isBlank() || schedule.isBlank()) {
            _actionState.value = UiState.Error("Iltimos, barcha maydonlarni to'ldiring")
            return
        }

        _actionState.value = UiState.Loading
        viewModelScope.launch {
            val newGroup = Group(
                id = "group_" + UUID.randomUUID().toString().take(6),
                name = name,
                teacherId = operator.id,
                schedule = schedule,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            eduRepository.createGroup(newGroup).collect { result ->
                result.onSuccess {
                    _actionState.value = UiState.Success(Unit)
                }.onFailure { e ->
                    _actionState.value = UiState.Error(e.localizedMessage ?: "Guruh yaratishda xatolik yuz berdi")
                }
            }
        }
    }

    fun approveStudent(student: User) {
        _actionState.value = UiState.Loading
        viewModelScope.launch {
            val approved = student.copy(
                status = UserStatus.ACTIVE,
                updatedAt = System.currentTimeMillis()
            )
            eduRepository.updateStudent(approved).collect { result ->
                result.onSuccess {
                    _actionState.value = UiState.Success(Unit)
                }.onFailure { e ->
                    _actionState.value = UiState.Error(e.localizedMessage ?: "Talabani tasdiqlashda xatolik yuz berdi")
                }
            }
        }
    }
}
