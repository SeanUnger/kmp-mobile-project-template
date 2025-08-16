package util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun currentDateVersion(): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("America/New_York")
    return formatter.format(Date())
}