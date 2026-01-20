package domain.service.result.internals

import y9to.libs.stdlib.Union
import y9to.libs.stdlib.mapSuccess as normalMapSuccess
import y9to.libs.stdlib.mapError as normalMapError


internal fun <S, E, NS, NE> Union<S, E>.mapBoth(success: S.() -> NS, error: E.() -> NE): Union<NS, NE> =
    normalMapSuccess { it.success() }.normalMapError { it.error() }

internal fun <S, E, NS> Union<S, E>.mapSuccess(transform: S.() -> NS): Union<NS, E> =
    normalMapSuccess { it.transform() }

internal fun <S, E, NE> Union<S, E>.mapError(transform: E.() -> NE): Union<S, NE> =
    normalMapError { it.transform() }
