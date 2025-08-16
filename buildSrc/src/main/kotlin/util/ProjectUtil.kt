package util

import org.gradle.api.Project

fun Project.getAppName(): String {
    return findProperty("appName") as String? ?: "myapp"
} 