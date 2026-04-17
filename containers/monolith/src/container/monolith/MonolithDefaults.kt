package container.monolith

import kotlin.error


object MonolithDefaults {
    val authSecret = System.getenv("auth_secret") ?: error("auth_secret environment variable is required")
    val host = System.getenv("ktor_host") ?: "0.0.0.0"
    val port = System.getenv("ktor_port")?.toInt() ?: 8103
    // localhost:6379
    val redisUrl = System.getenv("redis_url") ?: error("redis_url environment variable is required")
    // localhost:9092
    val kafkaUrl = System.getenv("kafka_url") ?: error("kafka_url environment variable is required")
    // r2dbc:postgresql://user:password@host:port/database
    val postgresUrl = System.getenv("postgres_url") ?: error("postgres_url environment variable is required")
    val fileGatewayAddress = System.getenv("ktor_file_gateway_address") ?: "http://localhost:$port"
    // /home/user/server/files
    val filesDirectory = System.getenv("y9to_files_directory") ?: error("y9to_files_directory environment variable is required")
}
