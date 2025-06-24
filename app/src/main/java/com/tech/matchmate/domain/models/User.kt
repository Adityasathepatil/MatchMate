package com.tech.matchmate.domain.models

import com.tech.matchmate.domain.models.enums.MatchStatus

data class User(
    val id: String,
    val name: String,
    val age: Int,
    val city: String,
    val imageUrl: String,
    val email: String,
    val education: String,
    val profession: String,
    val matchScore: Int = 0,
    val status: MatchStatus = MatchStatus.PENDING
)
