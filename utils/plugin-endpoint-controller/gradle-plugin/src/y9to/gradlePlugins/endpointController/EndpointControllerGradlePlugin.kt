package y9to.gradlePlugins.endpointController

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption


class EndpointControllerGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    override fun getCompilerPluginId(): String {
        return "y9to.compilerPlugins.endpointController"
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = "me.maly.y9to",
            artifactId = "compiler-plugin-endpoint-controller",
            version = "1.0-SNAPSHOT"
        )
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            emptyList()
        }
    }
}
