package y9to.compilerPlugins.endpointController.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.utils.AbstractSimpleClassPredicateMatchingService
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName


class PrefixGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

}
