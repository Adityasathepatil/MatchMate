package com.tech.matchmate.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val city: String,
    val imageUrl: String,
    val email: String,
    val education: String,
    val profession: String,
    val matchScore: Int,
    val status: String
)
