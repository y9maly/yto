package presentation.infra.jwtManager


sealed interface IssueTokensResult {
    data class Ok(
        val accessToken: String,
        val refreshToken: String,
    ) : IssueTokensResult

    data object InvalidSessionId : IssueTokensResult
}

fun IssueTokensResult.asOk() = this as IssueTokensResult.Ok
