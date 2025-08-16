package configuration

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import util.extractFileName
import util.extractZipName
import java.io.File


fun Project.configureFrameworkDependencies(frameworkConfigs: List<FrameworkDependencyConfig>) {
    if (frameworkConfigs.isNotEmpty()) {
        pluginManager.apply("de.undercouch.download")

        frameworkConfigs.forEach { frameworkConfig ->
            configureXcFrameworkDownload(frameworkConfig)
            configureCInterop(frameworkConfig)
        }
    }
}

fun Project.configureXcFrameworkDownload(frameworkConfig: FrameworkDependencyConfig) {
    val zipName = frameworkConfig.zipUrl.extractZipName()
    val zipFile = frameworkConfig.zipUrl.extractFileName()

    val downloadFrameworkTaskName = "download${zipName}XcFramework"
    val unzipFrameworkTaskName = "unzip${zipName}XcFramework"

    val buildDir = project.layout.buildDirectory.get()

    // Downloads the source .zip file and places it in the build folder
    tasks.register(downloadFrameworkTaskName, Download::class.java) {
        src(frameworkConfig.zipUrl.toString())
        dest(buildDir)
        overwrite(true)
    }

    // Unzips the downloaded .zip file
    tasks.register(unzipFrameworkTaskName, Copy::class.java) {
        dependsOn(downloadFrameworkTaskName)
        from(zipTree("$buildDir/$zipFile"))
        into("$buildDir/$zipName")
    }

    // Ensures the downloading and unzipping tasks happen before the cinterop tasks, which generate kotlin code based on the .framework files.
    afterEvaluate {
        tasks.matching { it.name.startsWith("cinterop$zipName") }.configureEach {
            dependsOn(tasks.named(unzipFrameworkTaskName))
        }
    }
}

fun Project.configureCInterop(frameworkConfig: FrameworkDependencyConfig) {
    extensions.configure(KotlinMultiplatformExtension::class.java) {
        val zipName = frameworkConfig.zipUrl.extractZipName()

        iosTargets().forEach { iosTarget ->
            iosTarget.compilations.getByName("main") {
                // Create the cinterop task for this xcframework and target. This Task will later generate a kotlin interface based on objective-c code in the xcframework
                cinterops.create(zipName) {
                    // .def file path, required by the cinterop task. The def file specifies additional properties about the framework (in addition to compilerOpts and linkerOpts defined below).
                    defFile(frameworkConfig.defFile)

                    val absoluteXcFrameworkPath = "${layout.buildDirectory}/$zipName/${frameworkConfig.xcFrameworkPath}"

                    val xcFrameworkArchitectures = listOf("ios-arm64", "ios-arm64_x86_64-simulator")
                    xcFrameworkArchitectures.forEach { architecture ->
                        val architecturePath = "$absoluteXcFrameworkPath/$architecture"

                        // compiler and linker options are specified paths to the framework files within the xcframework, required by the cinterop task
                        compilerOpts("-F$architecturePath", "-framework", frameworkConfig.frameworkName)
                        linkerOpts("-F$architecturePath", "-framework", frameworkConfig.frameworkName)

                        if (defFileUsesModules(frameworkConfig.defFile)) {
                            // required when framework module mappings include clang features. Otherwise build will fail.
                            compilerOpts("-fmodules")
                        } else {
                            // This is adding the Headers folder to the path in case the framework config's .def file specifies certain header file to include (Eg headers=AppsFlyerLib.h).
                            includeDirs {
                                allHeaders(
                                    "$architecturePath/${frameworkConfig.frameworkName}.framework/Headers"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun defFileUsesModules(defFile: File): Boolean {
    if (!defFile.exists()) return false
    return defFile.readLines().any { line ->
        line.trim().startsWith("modules") || line.trim().startsWith("moduleName")
    }
}