package com.rpn.salatetime.utils

// Central icon definitions (All icons will be declared here, then used from here)
object Constants {
    // Predefined offset times for image display (moves business logic to ViewModel)
     val beforePrayerOffsets = listOf(30L, 10L, 5L, 3L, 1L) // 1st to 5th image
     val afterPrayerOffsets = listOf(5L, 7L, 10L, 15L, 30L)   // Last 5 images

    val DEFAULT_MOSQUE_NAME = "Masjid al-Noor"
    val DEFAULT_MOSQUE_MESSAGE =
        "Welcome to the House of Allah – Please maintain silence and respect for worship."
}

// Define the extensions
fun List<Long>.toNegativeOffsets(): List<Int> = this.map { -it.toInt() }
fun List<Long>.toIntOffsets(): List<Int> = this.map { it.toInt() }
