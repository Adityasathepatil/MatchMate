package com.tech.matchmate.utils

import com.tech.matchmate.data.local.database.entities.UserEntity
import com.tech.matchmate.domain.models.User
import com.tech.matchmate.domain.models.enums.MatchStatus

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        age = age,
        city = city,
        imageUrl = imageUrl,
        email = email,
        education = education,
        profession = profession,
        matchScore = matchScore,
        status = status.name
    )
}

fun UserEntity.toUser(): User {
    return User(
        id = id,
        name = name,
        age = age,
        city = city,
        imageUrl = imageUrl,
        email = email,
        education = education,
        profession = profession,
        matchScore = matchScore,
        status = MatchStatus.valueOf(status)
    )
}