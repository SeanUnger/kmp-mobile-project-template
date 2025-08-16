import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

fun Project.configureCompose() {
    pluginManager.apply(ComposeCompilerGradleSubplugin::class.java)
    pluginManager.apply(ComposePlugin::class.java)
    extensions.findByType(CommonExtension::class.java)?.also {
        it.buildFeatures {
            compose = true
        }
    }
    val composeDependencies = ComposePlugin.Dependencies(this)
    kotlinExtension.sourceSets.named("commonMain") {
        dependencies {
            implementation(composeDependencies.foundation)
            implementation(composeDependencies.material3)
            implementation(composeDependencies.materialIconsExtended)
            implementation(composeDependencies.runtime)
            implementation(ComposePlugin.CommonComponentsDependencies.resources)
        }
    }
}