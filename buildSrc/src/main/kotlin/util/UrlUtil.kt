package util

import java.net.URI
import java.net.URL

fun URL.extractZipName(): String {
    val filename = path.substringAfterLast('/').removeSuffix(".zip")

    val lower = filename.lowercase()
    return when {
        lower.endsWith(".xcframework") -> filename.dropLast(".xcframework".length)
        lower.endsWith("-xcframeworks") -> filename.dropLast("-xcframeworks".length)
        else -> filename
    }
}

fun URL.extractFileName(): String = path.substringAfterLast('/')

/**
 * Convenience for turning a String into a URL now that URL() constructor is deprecated.
 */
fun String.toUrl() = URI(this).toURL()