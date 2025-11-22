package com.rpn.mosquetime.utils

import android.content.Context

class AppStringProvider(private val context: Context) {
    fun getString(id: Int): String = context.getString(id)
    fun getString(id: Int, vararg formatArgs: Any): String = context.getString(id, *formatArgs)
}
