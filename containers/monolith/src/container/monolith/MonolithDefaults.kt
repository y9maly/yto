package container.monolith


object MonolithDefaults {
    val host = System.getenv("ktor_host") ?: "0.0.0.0"
    val port = System.getenv("ktor_port")?.toInt() ?: 8103
    // r2dbc:postgresql://user:password@host:port/database
    val postgresUrl = System.getenv("postgres_url") ?: error("postgres_url environment variable is required")
    val fileGatewayAddress = System.getenv("ktor_file_gateway_address") ?: "http://localhost:$port"
    // /home/user/server/files
    val filesDirectory = System.getenv("y9to_files_directory") ?: error("y9to_files_directory environment variable is required")
}
