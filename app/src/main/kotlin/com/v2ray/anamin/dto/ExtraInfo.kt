package com.v2ray.anamin.dto

import androidx.annotation.Nullable

class ExtraInfo {
    var up: String? = null
    var down: String? = null

    fun sumUpAndDown(): Double {
        val  data:Map<String,String?> = mapOf(
            "up" to up,
            "down" to down
        )
        val upValue = data["up"]?.split(" ")?.get(0)?.toDouble() ?: 0.0
        val upUnit = data["up"]?.split(" ")?.get(1) ?: "KB"

        val downValue = data["down"]?.split(" ")?.get(0)?.toDouble() ?: 0.0
        val downUnit = data["down"]?.split(" ")?.get(1) ?: "KB"

        val upInKB = when (upUnit) {
            "GB" -> upValue * 1_000_000
            "MB" -> upValue * 1_000
            else -> upValue
        }

        val downInKB = when (downUnit) {
            "GB" -> downValue * 1_000_000
            "MB" -> downValue * 1_000
            else -> downValue
        }

        val totalInKB = upInKB + downInKB
        return totalInKB
    }
    fun sumUpAndDownFormat(totalInKB:Double = sumUpAndDown()):String{

        return if (totalInKB >= 1_000_000) {
            val totalInGB = totalInKB / 1_000_000
            "%.2f GB".format(totalInGB)
        } else if (totalInKB >= 1_000) {
            val totalInMB = totalInKB / 1_000
            "%.2f MB".format(totalInMB)
        } else {
            "%.2f KB".format(totalInKB)
        }
    }
}
