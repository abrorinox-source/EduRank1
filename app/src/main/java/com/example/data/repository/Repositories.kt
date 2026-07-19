package com.example.data.repository

import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task
import java.util.UUID

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Unknown Task Exception"))
        }
    }
}

interface AuthRepository {
    fun login(email: String, password: String): Flow<Result<User>>
    fun signUp(email: String, password: String, fullName: String, role: UserRole, groupId: String?): Flow<Result<User>>
    fun getCurrentUser(): StateFlow<User?>
    fun logout(): Flow<Result<Unit>>
    fun isUsingMockMode(): Boolean
    fun setForceMockMode(enabled: Boolean)
}

interface EduRepository {
    fun getGroups(): Flow<List<Group>>
    fun getGroup(groupId: String): Flow<Group?>
    fun createGroup(group: Group): Flow<Result<Unit>>
    fun getStudents(groupId: String? = null): Flow<List<User>>
    fun getStudent(studentId: String): Flow<User?>
    fun createStudent(student: User): Flow<Result<Unit>>
    fun updateStudent(student: User): Flow<Result<Unit>>
    fun getTransactions(userId: String? = null): Flow<List<PointTransaction>>
    fun performTransfer(fromUserId: String, toUserId: String, amount: Int, note: String, createdBy: String): Flow<Result<Unit>>
    fun performAdjustment(targetUserId: String, amount: Int, type: TransactionType, note: String, createdBy: String): Flow<Result<Unit>>
}

class AuthRepositoryImpl(
    private val firestore: FirebaseFirestore?,
    private val firebaseAuth: FirebaseAuth?,
    private val mockDb: MockDatabase
) : AuthRepository {

    private val currentUserState = MutableStateFlow<User?>(null)
    private var forceMockMode = false

    init {
        // Observe auth state if Firebase is available
        firebaseAuth?.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Fetch user from Firestore
                firestore?.collection("users")?.document(firebaseUser.uid)
                    ?.get()
                    ?.addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val user = User.fromMap(doc.data ?: emptyMap()).copy(id = doc.id)
                            currentUserState.value = user
                        } else {
                            // Create standard student profile if not exists in firestore
                            val newUser = User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                fullName = firebaseUser.displayName ?: "New User",
                                role = UserRole.STUDENT
                            )
                            firestore.collection("users").document(firebaseUser.uid)
                                .set(newUser.toMap())
                            currentUserState.value = newUser
                        }
                    }
            } else {
                currentUserState.value = null
            }
        }
    }

    override fun isUsingMockMode(): Boolean {
        return false
    }

    override fun setForceMockMode(enabled: Boolean) {
        forceMockMode = enabled
    }

    override fun login(email: String, password: String): Flow<Result<User>> = flow {
        if (isUsingMockMode()) {
            val user = mockDb.authenticate(email, password)
            if (user != null) {
                currentUserState.value = user
                emit(Result.success(user))
            } else {
                emit(Result.failure(Exception("Invalid email or password")))
            }
            return@flow
        }

        try {
            // Firebase Login
            val authResult = callbackFlow<Result<User>> {
                firebaseAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: ""
                        firestore!!.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val user = User.fromMap(doc.data ?: emptyMap()).copy(id = doc.id)
                                    currentUserState.value = user
                                    trySend(Result.success(user))
                                } else {
                                    val newUser = User(id = uid, email = email, role = UserRole.STUDENT)
                                    firestore.collection("users").document(uid).set(newUser.toMap())
                                    currentUserState.value = newUser
                                    trySend(Result.success(newUser))
                                }
                            }
                            .addOnFailureListener { e ->
                                trySend(Result.failure(e))
                            }
                    }
                    .addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
                awaitClose()
            }
            
            // Collect the single result
            authResult.collect { emit(it) }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signUp(email: String, password: String, fullName: String, role: UserRole, groupId: String?): Flow<Result<User>> = flow {
        if (isUsingMockMode()) {
            val existing = mockDb.usersFlow.value.find { it.email.lowercase() == email.trim().lowercase() }
            if (existing != null) {
                emit(Result.failure(Exception("Foydalanuvchi allaqachon mavjud (Mock)")))
                return@flow
            }
            val newUser = User(
                id = "mock_${UUID.randomUUID()}",
                email = email.trim(),
                fullName = fullName.trim(),
                role = role,
                groupId = if (role == UserRole.STUDENT) groupId else null,
                points = if (role == UserRole.STUDENT) 100 else 0,
                status = if (role == UserRole.STUDENT) UserStatus.PENDING else UserStatus.ACTIVE
            )
            mockDb.addUser(newUser)
            currentUserState.value = newUser
            emit(Result.success(newUser))
            return@flow
        }

        try {
            val authResult = callbackFlow<Result<User>> {
                firebaseAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: ""
                        val newUser = User(
                            id = uid,
                            email = email.trim(),
                            fullName = fullName.trim(),
                            role = role,
                            groupId = if (role == UserRole.STUDENT) groupId else null,
                            points = if (role == UserRole.STUDENT) 100 else 0,
                            status = if (role == UserRole.STUDENT) UserStatus.PENDING else UserStatus.ACTIVE
                        )
                        firestore!!.collection("users").document(uid).set(newUser.toMap())
                            .addOnSuccessListener {
                                currentUserState.value = newUser
                                trySend(Result.success(newUser))
                            }
                            .addOnFailureListener { e ->
                                trySend(Result.failure(e))
                            }
                    }
                    .addOnFailureListener { e ->
                        trySend(Result.failure(e))
                    }
                awaitClose()
            }
            authResult.collect { emit(it) }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getCurrentUser(): StateFlow<User?> {
        return currentUserState
    }

    override fun logout(): Flow<Result<Unit>> = flow {
        if (isUsingMockMode()) {
            currentUserState.value = null
            emit(Result.success(Unit))
            return@flow
        }
        try {
            firebaseAuth?.signOut()
            currentUserState.value = null
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

class EduRepositoryImpl(
    private val firestore: FirebaseFirestore?,
    private val authRepository: AuthRepository,
    private val mockDb: MockDatabase
) : EduRepository {

    private fun isMock(): Boolean = authRepository.isUsingMockMode()

    override fun getGroups(): Flow<List<Group>> = callbackFlow {
        if (isMock()) {
            mockDb.groupsFlow.collect { trySend(it) }
            awaitClose()
            return@callbackFlow
        }

        val listener = firestore!!.collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("EduRepository", "Firestore error in getGroups: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val groups = snapshot?.documents?.map { doc ->
                    Group.fromMap(doc.data ?: emptyMap()).copy(id = doc.id)
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    override fun getGroup(groupId: String): Flow<Group?> = callbackFlow {
        if (isMock()) {
            mockDb.groupsFlow.collect { list ->
                trySend(list.find { it.id == groupId })
            }
            awaitClose()
            return@callbackFlow
        }

        val listener = firestore!!.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("EduRepository", "Firestore error in getGroup($groupId): ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val group = snapshot?.let { doc ->
                    doc.data?.let { Group.fromMap(it).copy(id = doc.id) }
                }
                trySend(group)
            }
        awaitClose { listener.remove() }
    }

    override fun createGroup(group: Group): Flow<Result<Unit>> = flow {
        if (isMock()) {
            mockDb.addGroup(group)
            emit(Result.success(Unit))
            return@flow
        }
        try {
            val id = group.id.ifEmpty { UUID.randomUUID().toString() }
            val finalGroup = group.copy(id = id)
            firestore!!.collection("groups").document(id).set(finalGroup.toMap()).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudents(groupId: String?): Flow<List<User>> = callbackFlow {
        if (isMock()) {
            mockDb.usersFlow.collect { users ->
                val students = users.filter { it.role == UserRole.STUDENT }
                if (groupId != null) {
                    trySend(students.filter { it.groupId == groupId })
                } else {
                    trySend(students)
                }
            }
            awaitClose()
            return@callbackFlow
        }

        val query = if (groupId != null) {
            firestore!!.collection("users")
                .whereEqualTo("role", "student")
                .whereEqualTo("groupId", groupId)
        } else {
            firestore!!.collection("users")
                .whereEqualTo("role", "student")
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("EduRepository", "Firestore error in getStudents: ${error.message}", error)
                close(error)
                return@addSnapshotListener
            }
            val students = snapshot?.documents?.map { doc ->
                User.fromMap(doc.data ?: emptyMap()).copy(id = doc.id)
            } ?: emptyList()
            trySend(students)
        }
        awaitClose { listener.remove() }
    }

    override fun getStudent(studentId: String): Flow<User?> = callbackFlow {
        if (isMock()) {
            mockDb.usersFlow.collect { list ->
                trySend(list.find { it.id == studentId })
            }
            awaitClose()
            return@callbackFlow
        }

        val listener = firestore!!.collection("users").document(studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("EduRepository", "Firestore error in getStudent($studentId): ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.let { doc ->
                    doc.data?.let { User.fromMap(it).copy(id = doc.id) }
                }
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    override fun createStudent(student: User): Flow<Result<Unit>> = flow {
        if (isMock()) {
            mockDb.addUser(student.copy(role = UserRole.STUDENT))
            emit(Result.success(Unit))
            return@flow
        }
        try {
            val id = student.id.ifEmpty { UUID.randomUUID().toString() }
            val finalStudent = student.copy(id = id, role = UserRole.STUDENT)
            firestore!!.collection("users").document(id).set(finalStudent.toMap()).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateStudent(student: User): Flow<Result<Unit>> = flow {
        if (isMock()) {
            mockDb.updateUser(student)
            emit(Result.success(Unit))
            return@flow
        }
        try {
            firestore!!.collection("users").document(student.id).set(student.toMap()).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getTransactions(userId: String?): Flow<List<PointTransaction>> = callbackFlow {
        if (isMock()) {
            mockDb.transactionsFlow.collect { txs ->
                if (userId != null) {
                    trySend(txs.filter { it.fromUserId == userId || it.toUserId == userId })
                } else {
                    trySend(txs)
                }
            }
            awaitClose()
            return@callbackFlow
        }

        val query = firestore!!.collection("point_transactions")
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("EduRepository", "Firestore error in getTransactions: ${error.message}", error)
                close(error)
                return@addSnapshotListener
            }
            val txs = snapshot?.documents?.map { doc ->
                PointTransaction.fromMap(doc.data ?: emptyMap()).copy(id = doc.id)
            } ?: emptyList()
            
            // Filter client side to avoid complex firestore indexes for simple query matching
            val filteredTxs = if (userId != null) {
                txs.filter { it.fromUserId == userId || it.toUserId == userId }
            } else {
                txs
            }
            trySend(filteredTxs.sortedByDescending { it.createdAt })
        }
        awaitClose { listener.remove() }
    }

    override fun performTransfer(
        fromUserId: String,
        toUserId: String,
        amount: Int,
        note: String,
        createdBy: String
    ): Flow<Result<Unit>> = flow {
        if (isMock()) {
            val success = mockDb.transferPoints(fromUserId, toUserId, amount, note, createdBy)
            if (success) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Insufficient points or invalid users")))
            }
            return@flow
        }

        try {
            val result = callbackFlow<Result<Unit>> {
                firestore!!.runTransaction { transaction ->
                    val fromRef = firestore.collection("users").document(fromUserId)
                    val toRef = firestore.collection("users").document(toUserId)

                    val fromUserDoc = transaction.get(fromRef)
                    val toUserDoc = transaction.get(toRef)

                    val fromPoints = (fromUserDoc.get("points") as? Number)?.toInt() ?: 0
                    val toPoints = (toUserDoc.get("points") as? Number)?.toInt() ?: 0

                    if (fromPoints < amount) {
                        throw Exception("Insufficient points")
                    }

                    // Deduct and Add
                    transaction.update(fromRef, "points", fromPoints - amount)
                    transaction.update(toRef, "points", toPoints + amount)

                    // Write Transaction
                    val txId = UUID.randomUUID().toString()
                    val tx = PointTransaction(
                        id = txId,
                        fromUserId = fromUserId,
                        toUserId = toUserId,
                        amount = amount,
                        type = TransactionType.TRANSFER,
                        createdBy = createdBy,
                        note = note
                    )
                    transaction.set(firestore.collection("point_transactions").document(txId), tx.toMap())
                }.addOnSuccessListener {
                    trySend(Result.success(Unit))
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
                awaitClose()
            }
            result.collect { emit(it) }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun performAdjustment(
        targetUserId: String,
        amount: Int,
        type: TransactionType,
        note: String,
        createdBy: String
    ): Flow<Result<Unit>> = flow {
        if (isMock()) {
            val success = mockDb.adjustPoints(targetUserId, amount, type, note, createdBy)
            if (success) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Adjustment failed")))
            }
            return@flow
        }

        try {
            val result = callbackFlow<Result<Unit>> {
                firestore!!.runTransaction { transaction ->
                    val userRef = firestore.collection("users").document(targetUserId)
                    val userDoc = transaction.get(userRef)
                    val currentPoints = (userDoc.get("points") as? Number)?.toInt() ?: 0

                    val newPoints = when (type) {
                        TransactionType.MANUAL_ADD -> currentPoints + amount
                        TransactionType.MANUAL_SUBTRACT -> {
                            val pts = currentPoints - amount
                            if (pts < 0) 0 else pts // clamp to 0 or allow negative? Standard is clamping to 0 or warning. Clamped to 0.
                        }
                        else -> currentPoints
                    }

                    transaction.update(userRef, "points", newPoints)

                    // Write Transaction
                    val txId = UUID.randomUUID().toString()
                    val tx = PointTransaction(
                        id = txId,
                        fromUserId = null,
                        toUserId = targetUserId,
                        amount = amount,
                        type = type,
                        createdBy = createdBy,
                        note = note
                    )
                    transaction.set(firestore.collection("point_transactions").document(txId), tx.toMap())
                }.addOnSuccessListener {
                    trySend(Result.success(Unit))
                }.addOnFailureListener { e ->
                    trySend(Result.failure(e))
                }
                awaitClose()
            }
            result.collect { emit(it) }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
