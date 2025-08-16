plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.compose.gradle)
    implementation(libs.compose.compiler)
    implementation(libs.android.application)
    implementation(libs.undercouch.gradle.download.task)
}

gradlePlugin {
    plugins {
        register("sungerconventionplugin") {
            id = "sunger.convention"
            implementationClass = "plugin.SungerConventionPlugin"
        }
    }
}