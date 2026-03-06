package y9to.compilerPlugins.endpointController

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import y9to.compilerPlugins.endpointController.fir.ControllerGenerationExtension
import y9to.compilerPlugins.endpointController.fir.PrefixGenerator


class EndpointControllerPluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
//        +::ControllerGenerationExtension
        +::PrefixGenerator
    }
}
