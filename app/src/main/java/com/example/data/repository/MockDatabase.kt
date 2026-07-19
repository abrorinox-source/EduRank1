package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MockDatabase {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val usersFlow: StateFlow<List<User>> = _users.asStateFlow()

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groupsFlow: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _transactions = MutableStateFlow<List<PointTransaction>>(emptyList())
    val transactionsFlow: StateFlow<List<PointTransaction>> = _transactions.asStateFlow()

    init {
        seedData()
    }

    private fun seedData() {
        // Create initial Groups
        val groupA = Group(id = "group_10a", name = "Group 10-A", schedule = "Mon/Wed 15:00", teacherId = "teacher_1")
        val groupB = Group(id = "group_09c", name = "Group 09-C", schedule = "Tue/Thu 16:30", teacherId = "teacher_1")
        val groupC = Group(id = "group_10b", name = "Group 10-B", schedule = "Fri 14:00", teacherId = "teacher_2")
        _groups.value = listOf(groupA, groupB, groupC)

        // Create Users: Admins, Teachers, Students
        val admin = User(
            id = "admin_1",
            fullName = "Jane Doe",
            email = "admin@edurank.com",
            phone = "+123456789",
            role = UserRole.ADMIN,
            status = UserStatus.ACTIVE
        )

        val teacher = User(
            id = "teacher_1",
            fullName = "John Davis",
            email = "teacher@edurank.com",
            phone = "+987654321",
            role = UserRole.TEACHER,
            status = UserStatus.ACTIVE
        )

        // Students (aligned with Professional Polish design example)
        val s1 = User(
            id = "student_alex",
            fullName = "Alex Johnson",
            email = "alex@edurank.com",
            phone = "+100000001",
            role = UserRole.STUDENT,
            groupId = "group_10a",
            points = 1420,
            status = UserStatus.ACTIVE
        )

        val s2 = User(
            id = "student_sarah",
            fullName = "Sarah Williams",
            email = "sarah@edurank.com",
            phone = "+100000002",
            role = UserRole.STUDENT,
            groupId = "group_10a",
            points = 1385,
            status = UserStatus.ACTIVE
        )

        val s3 = User(
            id = "student_michael",
            fullName = "Michael Ross",
            email = "michael@edurank.com",
            phone = "+100000003",
            role = UserRole.STUDENT,
            groupId = "group_09c",
            points = 920,
            status = UserStatus.ACTIVE
        )

        val s4 = User(
            id = "student_emma",
            fullName = "Emma Watson",
            email = "emma@edurank.com",
            phone = "+100000004",
            role = UserRole.STUDENT,
            groupId = "group_10a",
            points = 450,
            status = UserStatus.ACTIVE
        )

        val s5 = User(
            id = "student_john",
            fullName = "John Doe",
            email = "john@edurank.com",
            phone = "+100000005",
            role = UserRole.STUDENT,
            groupId = "group_10b",
            points = 1100,
            status = UserStatus.ACTIVE
        )

        _users.value = listOf(admin, teacher, s1, s2, s3, s4, s5)

        // Add some mock transactions to display
        val tx1 = PointTransaction(
            id = "tx_1",
            fromUserId = null,
            toUserId = "student_alex",
            amount = 100,
            type = TransactionType.MANUAL_ADD,
            createdBy = "teacher_1",
            note = "Excellent Homework presentation on Integration",
            createdAt = System.currentTimeMillis() - 3600000 * 24 // 1 day ago
        )

        val tx2 = PointTransaction(
            id = "tx_2",
            fromUserId = "student_alex",
            toUserId = "student_sarah",
            amount = 50,
            type = TransactionType.TRANSFER,
            createdBy = "student_alex",
            note = "Thanks for sharing the Physics study guide!",
            createdAt = System.currentTimeMillis() - 3600000 * 12 // 12 hours ago
        )

        val tx3 = PointTransaction(
            id = "tx_3",
            fromUserId = null,
            toUserId = "student_emma",
            amount = 30,
            type = TransactionType.MANUAL_SUBTRACT,
            createdBy = "teacher_1",
            note = "Late submission of homework 4",
            createdAt = System.currentTimeMillis() - 3600000 * 2 // 2 hours ago
        )

        _transactions.value = listOf(tx1, tx2, tx3)
    }

    fun authenticate(email: String, password: String): User? {
        val trimmedEmail = email.trim().lowercase()
        // For simplicity in mock testing, any user in our system can log in.
        // We'll map emails to their respective records.
        // If password is not blank, allow login.
        if (password.isBlank()) return null
        return _users.value.find { it.email.lowercase() == trimmedEmail }
    }

    fun addGroup(group: Group) {
        val id = group.id.ifEmpty { "group_${UUID.randomUUID()}" }
        val finalGroup = group.copy(id = id)
        _groups.value = _groups.value + finalGroup
    }

    fun addUser(user: User) {
        val id = user.id.ifEmpty { "user_${UUID.randomUUID()}" }
        val finalUser = user.copy(id = id)
        _users.value = _users.value + finalUser
    }

    fun updateUser(user: User) {
        _users.value = _users.value.map {
            if (it.id == user.id) user else it
        }
    }

    fun transferPoints(fromUserId: String, toUserId: String, amount: Int, note: String, createdBy: String): Boolean {
        val usersList = _users.value
        val fromUser = usersList.find { it.id == fromUserId } ?: return false
        val toUser = usersList.find { it.id == toUserId } ?: return false

        if (fromUser.points < amount) return false

        // Perform atomic update
        _users.value = usersList.map {
            when (it.id) {
                fromUserId -> it.copy(points = it.points - amount, updatedAt = System.currentTimeMillis())
                toUserId -> it.copy(points = it.points + amount, updatedAt = System.currentTimeMillis())
                else -> it
            }
        }

        // Add transaction
        val tx = PointTransaction(
            id = "tx_${UUID.randomUUID()}",
            fromUserId = fromUserId,
            toUserId = toUserId,
            amount = amount,
            type = TransactionType.TRANSFER,
            createdBy = createdBy,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }

    fun adjustPoints(targetUserId: String, amount: Int, type: TransactionType, note: String, createdBy: String): Boolean {
        val usersList = _users.value
        val targetUser = usersList.find { it.id == targetUserId } ?: return false

        val newPoints = when (type) {
            TransactionType.MANUAL_ADD -> targetUser.points + amount
            TransactionType.MANUAL_SUBTRACT -> {
                val valSub = targetUser.points - amount
                if (valSub < 0) 0 else valSub
            }
            else -> targetUser.points
        }

        _users.value = usersList.map {
            if (it.id == targetUserId) {
                it.copy(points = newPoints, updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        }

        val tx = PointTransaction(
            id = "tx_${UUID.randomUUID()}",
            fromUserId = null,
            toUserId = targetUserId,
            amount = amount,
            type = type,
            createdBy = createdBy,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }
}
