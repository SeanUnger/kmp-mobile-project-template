plugins {
    id("sunger.convention")
}

sunger {
    android()
    ios()
    compose()
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        iosMain.dependencies {

        }

        commonMain {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.kotlinx.datetime)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

