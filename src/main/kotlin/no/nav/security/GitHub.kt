package no.nav.security

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import java.net.URL

class GitHub constructor(private val http: HttpClient, private val accessToken: String? = null) {
    private val url = URL("https://api.github.com/graphql")
    private val requestBody = """
        {"query":"{ repos: search(query: \"user:navikt fork:false archived:false\", type: REPOSITORY, first: 1) { repositoryCount}}","variables":{}}        
    """.trimIndent()

    suspend fun repoCount(): Int {
        val rawResponse: JsonObject = http.post(url) {
            accessToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
            setBody(requestBody)
        }.body()
        val gqlResponse = GraphQLResponse(rawResponse)
        if (gqlResponse.hasErrors()) {
            throw RuntimeException(gqlResponse.errorMsgs().toString())
        }
        return gqlResponse.repoCount() ?: throw RuntimeException("unable to read repo count")
    }

}