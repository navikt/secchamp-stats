package no.nav.security

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("secchamp-stats")

fun main() = runBlocking {
    val gitHub = GitHub(httpCLient(), requiredFromEnv("GH_TOKEN"))
    logger.info("found ${gitHub.repoCount()} non-archived repos on gh")
}


@OptIn(ExperimentalSerializationApi::class)
private fun httpCLient() = HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json = Json { explicitNulls = false })
    }
}

private fun requiredFromEnv(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: throw RuntimeException("unable to find '$name' in environment")
