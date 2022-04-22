package no.nav.security

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GraphQLResponseTest {

    @Test
    fun `repo count is extracted from ok response`() {
        val okResponse = GraphQLResponse(rawResponse = okResponseRaw)
        assertEquals(1234, okResponse.repoCount())
    }

    @Test
    fun `ok response has no errors`() {
        val okResponse = GraphQLResponse(rawResponse = okResponseRaw)
        assertFalse(okResponse.hasErrors())
    }

    @Test
    fun `error response has errors`() {
        val errorResponse = GraphQLResponse(errorResponseRaw)
        assertTrue(errorResponse.hasErrors())
    }

    @Test
    fun `all error messages are carried forwards`() {
        val errorResponse = GraphQLResponse(errorResponseRaw)
        assertEquals(2, errorResponse.errorMsgs().size)
    }

    private val okResponseRaw: JsonObject = Json.decodeFromString("""
        {"data":{"repos":{"repositoryCount": 1234 }}}
    """.trimIndent())

    private val errorResponseRaw: JsonObject = Json.decodeFromString("""
        {"errors":[{"message":"oh noes"}, {"message":"not another one"}]}
    """.trimIndent())

}