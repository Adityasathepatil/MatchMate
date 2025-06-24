package com.tech.matchmate.data.remote.model

data class RandomUser(
    val name: Name,
    val location: Location,
    val email: String,
    val dob: Dob,
    val picture: Picture,
    val login: Login
)