package com.rpn.mosquetime.data.local.dto

data class Hijri(
    var date: String?=null,
    val month: Month?=null,
    val weekday: Weekday?=null,
    val year: String?=null
)

data class Month(
    val ar: String,
    val en: String,
    val number: Int
)
data class Weekday(
    val ar: String,
    val en: String
)