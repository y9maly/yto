package container.monolith

import kotlin.error


internal object MonolithDefaults {
    val authSecret = System.getenv("AUTH_SECRET") ?: error("AUTH_SECRET environment variable is required")
    val host = System.getenv("KTOR_HOST") ?: "0.0.0.0"
    val port = System.getenv("KTOR_PORT")?.toInt() ?: 8103
    // localhost:6379
    val redisUrl = System.getenv("REDIS_URL") ?: error("REDIS_URL environment variable is required")
    // localhost:9092
    val kafkaUrl = System.getenv("KAFKA_URL") ?: error("KAFKA_URL environment variable is required")
    // r2dbc:postgresql://user:password@host:port/database
    val postgresUrl = System.getenv("POSTGRES_URL") ?: error("POSTGRES_URL environment variable is required")
    val fileGatewayAddress = System.getenv("KTOR_FILE_GATEWAY_ADDRESS") ?: "http://localhost:$port"
    // /home/user/server/files
    val filesDirectory = System.getenv("YTO_FILES_DIRECTORY") ?: error("YTO_FILES_DIRECTORY environment variable is required")
}
