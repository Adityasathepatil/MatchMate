package com.tech.matchmate.data.repository

import com.tech.matchmate.data.local.database.dao.UserDao
import com.tech.matchmate.data.remote.api.ApiService
import com.tech.matchmate.data.remote.model.RandomUser
import com.tech.matchmate.domain.models.User
import com.tech.matchmate.domain.models.enums.MatchStatus
import com.tech.matchmate.utils.toEntity
import com.tech.matchmate.utils.toUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    private val educationList = listOf(
        "Bachelor's Degree", "Master's Degree", "PhD", "High School",
        "Diploma", "Professional Certificate", "MBA"
    )

    private val professionList = listOf(
        "Software Engineer", "Doctor", "Teacher", "Business Analyst",
        "Marketing Manager", "Consultant", "Designer", "Lawyer"
    )

    suspend fun fetchAndStoreUsers(): Result<List<User>> {
        return try {
            // Simulate network flakiness (30% failure rate)
            if (Random.nextFloat() < 0.3) {
                throw Exception("Network error - simulated flaky connection")
            }

            val response = apiService.getRandomUsers(10)
            val users = response.results.map { randomUser ->
                mapToUser(randomUser)
            }

            // Store in database
            val userEntities = users.map { it.toEntity() }
            userDao.insertUsers(userEntities)

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fixed: Ensure proper mapping from UserEntity to User
    suspend fun getCachedUsers(): List<User> {
        return userDao.getAllUsers().map { userEntity ->
            userEntity.toUser()
        }
    }

    suspend fun updateUserStatus(userId: String, status: MatchStatus) {
        userDao.updateUserStatus(userId, status.name)
    }

    private fun mapToUser(randomUser: RandomUser): User {
        val education = educationList.random()
        val profession = professionList.random()

        return User(
            id = randomUser.login.uuid,
            name = "${randomUser.name.first} ${randomUser.name.last}",
            age = randomUser.dob.age,
            city = randomUser.location.city,
            imageUrl = randomUser.picture.large,
            email = randomUser.email,
            education = education,
            profession = profession,
            matchScore = 0, // Will be calculated later
            status = MatchStatus.PENDING
        )
    }

    fun calculateMatchScore(user: User, currentUserAge: Int = 28, currentUserCity: String = "Mumbai"): Int {
        val ageProximityScore = when (abs(user.age - currentUserAge)) {
            in 0..2 -> 50
            in 3..5 -> 40
            in 6..10 -> 30
            in 11..15 -> 20
            else -> 10
        }

        val cityMatchScore = if (user.city.equals(currentUserCity, ignoreCase = true)) 50 else 0

        return ageProximityScore + cityMatchScore
    }
}