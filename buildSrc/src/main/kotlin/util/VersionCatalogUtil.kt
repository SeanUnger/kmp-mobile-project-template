package util

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

fun Project.versionCatalog() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")