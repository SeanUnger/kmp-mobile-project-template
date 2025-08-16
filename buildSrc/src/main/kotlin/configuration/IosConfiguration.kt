package configuration

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import util.getAppName
import java.io.File
import java.net.URL

data class FrameworkDependencyConfig(
    val zipUrl: URL,
    val defFile: File,
    val xcFrameworkPath: String, // relative path within the exploded ZIP to the .xcframework file. For example "xcframeworks/Rudder.xcframework"
    val frameworkName: String, // Name of the .framework, which can be found inside the xcFramework. For example "Rudder" (for a Rudder.framework file)
)

fun KotlinMultiplatformExtension.iosTargets() = listOf(
    iosArm64(),
    iosSimulatorArm64()
)

fun Project.configureIos(
    frameworkConfigs: List<FrameworkDependencyConfig> = listOf()
) {
    configureIosTargetsAndSourceSets()
    configureFrameworkDependencies(frameworkConfigs)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
private fun Project.configureIosTargetsAndSourceSets() {
    extensions.configure(KotlinMultiplatformExtension::class.java) {
        iosTargets().forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = project.getAppName()
                isStatic = true
            }
        }

        sourceSets.create("iosMain") {
            dependsOn(sourceSets.commonMain.get())
            sourceSets.iosArm64Main.get().dependsOn(this)
            sourceSets.iosSimulatorArm64Main.get().dependsOn(this)
        }
    }
}