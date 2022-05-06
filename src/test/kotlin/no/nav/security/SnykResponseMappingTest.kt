package no.nav.security

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnykResponseMappingTest {

    @Test
    fun `numbers are preserved`() {
        val rawResponse = Json.parseToJsonElement("""{"results":[{"day":"2022-05-06","count":42859,"severity":{"critical":1402,"high":10461,"medium":16556,"low":14440}}]}""").jsonObject
        val expected = mapOf(
            "critical" to 1402,
            "high" to 10461,
            "medium" to 16556,
            "low" to 14440,
        )
        val actual = bySeverity(rawResponse)
        assertEquals(expected, actual)
    }

}