
package com.example.custodescommand.core.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromModelCounts(
        counts: Set<Int>
    ): String {
        return counts.sorted().joinToString(",")
    }

    @TypeConverter
    fun toModelCounts(
        value: String
    ): Set<Int> {
        if (value.isBlank()) {
            return emptySet()
        }

        return value
            .split(",")
            .map { it.toInt() }
            .toSet()
    }
}
