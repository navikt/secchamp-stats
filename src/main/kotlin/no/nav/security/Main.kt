package no.nav.security

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("secchamp-stats")

fun main() = runBlocking {
    val snyk = Snyk(httpCLient(), requiredFromEnv("SNYK_TOKEN"))
    val bq = BigQuery(requiredFromEnv("GCP_TEAM_PROJECT_ID"))
    val snykOrgs = snyk.orgs()
    val issues = snyk.issueCountsFor(snykOrgs)
    val rows = bq.insert(issues.map(::toRecord))
    logger.info("done, inserted $rows rows")
}

@OptIn(ExperimentalSerializationApi::class)
private fun httpCLient() = HttpClient(CIO) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        })
    }
}

private fun requiredFromEnv(name: String) =
    System.getProperty(name)
        ?: System.getenv(name)
        ?: throw RuntimeException("unable to find '$name' in environment")

private fun toRecord(project: Project) = IssueCountRecord(
    project = project.name,
    type = project.type,
    critical = project.issueCountsBySeverity.critical,
    high = project.issueCountsBySeverity.high,
    medium = project.issueCountsBySeverity.medium,
    low = project.issueCountsBySeverity.low
)
