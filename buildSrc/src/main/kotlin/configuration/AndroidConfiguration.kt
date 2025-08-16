package configuration

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import util.versionCatalog
import util.getAppName

fun Project.configureAndroidApplication() {
    pluginManager.apply("com.android.application")

    val appName = getAppName()
    
    extensions.configure(ApplicationExtension::class.java) {
        namespace = "com.sunger.$appName"
        compileSdk = versionCatalog().findVersion("android-compileSdk").get().toString().toInt()
        defaultConfig {
            targetSdk = versionCatalog().findVersion("android-targetSdk").get().toString().toInt()
            minSdk = versionCatalog().findVersion("android-minSdk").get().toString().toInt()
            versionName = project.findProperty("versionName") as String? ?: "1.0.0"
            versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 1
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
            }
        }
    }
}

fun Project.configureJvmKmp() {
    extensions.configure(KotlinMultiplatformExtension::class.java) {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
}