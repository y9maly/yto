@file:OptIn(ExperimentalCompilerApi::class)

package y9to.compilerPlugins.endpointController

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter


class EndpointControllerPluginComponentRegistrar : CompilerPluginRegistrar() {
    override val pluginId = "y9to.compilerPlugins.endpointController"
    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(EndpointControllerPluginRegistrar())
    }
}
