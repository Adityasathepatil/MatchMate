package com.tech.matchmate.data.local.database

import androidx.room.TypeConverter
import com.tech.matchmate.domain.models.enums.MatchStatus

class Converters {
    @TypeConverter
    fun fromMatchStatus(status: MatchStatus): String = status.name

    @TypeConverter
    fun toMatchStatus(status: String): MatchStatus = MatchStatus.valueOf(status)
}