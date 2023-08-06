package com.example.firebaseforum.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.myCapitalize(): String{
    return this.replaceFirstChar { firstLetter: Char -> firstLetter.uppercase() }
}
fun Long.toDateString(): String{
    val sfd = SimpleDateFormat(
        "dd.MM.yyyy HH:mm",
        Locale.getDefault()
    )
    return sfd.format(Date(this))
}