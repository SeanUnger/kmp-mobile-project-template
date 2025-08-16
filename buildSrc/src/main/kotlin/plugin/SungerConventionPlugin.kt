package plugin

import configuration.configureAndroidApplication
import configuration.configureIos
import configuration.FrameworkDependencyConfig
import configuration.configureJvmKmp
import configureCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class SungerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")

            extensions.create(
                "sunger",
                SungerPluginExtension::class.java,
                target,
            )
        }
    }
}

open class SungerPluginExtension(private val project: Project) {

    fun android() {
        project.configureAndroidApplication()
        project.configureJvmKmp()
    }


    fun ios(
        xcFrameworkInternalDependencies: List<FrameworkDependencyConfig> = listOf(), // xcframeworks to include as internal dependencies in the ios source set
    ) = project.configureIos(
        frameworkConfigs = xcFrameworkInternalDependencies,
    )

    fun compose() {
        project.configureCompose()
    }
}