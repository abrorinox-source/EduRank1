package com.example.data.model

import java.util.UUID

enum class UserRole {
    STUDENT, TEACHER, ADMIN;
    
    companion object {
        fun fromString(value: String?): UserRole {
            return when (value?.lowercase()) {
                "teacher" -> TEACHER
                "admin" -> ADMIN
                else -> STUDENT
            }
        }
    }
}

enum class UserStatus {
    ACTIVE, INACTIVE, PENDING;

    companion object {
        fun fromString(value: String?): UserStatus {
            return when (value?.lowercase()) {
                "inactive" -> INACTIVE
                "pending" -> PENDING
                else -> ACTIVE
            }
        }
    }
}

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.STUDENT,
    val groupId: String? = null,
    val points: Int = 0,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "role" to role.name.lowercase(),
            "groupId" to groupId,
            "points" to points,
            "status" to status.name.lowercase(),
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                role = UserRole.fromString(map["role"] as? String),
                groupId = map["groupId"] as? String,
                points = (map["points"] as? Number)?.toInt() ?: 0,
                status = UserStatus.fromString(map["status"] as? String),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class Group(
    val id: String = "",
    val name: String = "",
    val teacherId: String? = null,
    val schedule: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "teacherId" to teacherId,
            "schedule" to schedule,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Group {
            return Group(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                teacherId = map["teacherId"] as? String,
                schedule = map["schedule"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

enum class TransactionType {
    TRANSFER, MANUAL_ADD, MANUAL_SUBTRACT;

    companion object {
        fun fromString(value: String?): TransactionType {
            return when (value?.lowercase()) {
                "manual_add" -> MANUAL_ADD
                "manual_subtract" -> MANUAL_SUBTRACT
                else -> TRANSFER
            }
        }
    }
}

data class PointTransaction(
    val id: String = "",
    val fromUserId: String? = null,
    val toUserId: String = "",
    val amount: Int = 0,
    val type: TransactionType = TransactionType.TRANSFER,
    val createdBy: String = "", // Admin/Teacher/Student ID who initiated
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "amount" to amount,
            "type" to type.name.lowercase(),
            "createdBy" to createdBy,
            "note" to note,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): PointTransaction {
            return PointTransaction(
                id = map["id"] as? String ?: "",
                fromUserId = map["fromUserId"] as? String,
                toUserId = map["toUserId"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toInt() ?: 0,
                type = TransactionType.fromString(map["type"] as? String),
                createdBy = map["createdBy"] as? String ?: "",
                note = map["note"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
