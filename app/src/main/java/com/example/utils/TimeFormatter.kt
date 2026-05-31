package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    private val sdf = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.ENGLISH)

    fun format(timestamp: Long): String {
        return sdf.format(Date(timestamp))
    }
}
