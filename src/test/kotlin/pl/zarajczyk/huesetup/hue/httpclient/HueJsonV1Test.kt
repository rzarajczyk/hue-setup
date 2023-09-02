package pl.zarajczyk.huesetup.hue.httpclient

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HueJsonV1Test {

    private val json = HueJson()

    @Test
    fun `should parse object type`() {
        // given
        val response = """ { "test": "abc" } """

        // when
        val output = json.v1Deserialize(response, HTestAbc::class)

        // then
        output.test shouldBe "abc"
    }

    @Test
    fun `should parse success`() {
        // given
        val response = """[ { "success": {"id": 5} } ] """

        // when
        val output = json.v1Deserialize(response, HTestSuccess::class)

        // then
        output.value.size shouldBe 1
        output.value.first().success.id shouldBe 5
    }

    @Test
    fun `should parse error type`() {
        // given
        val response = """
            [
                {
                    "error": {
                        "type": 5,
                        "address": "test-address",
                         "description": "test-description"
                    }
                }
            ]
        """.trimIndent()

        // when
        val exception = assertThrows<HueErrorResponse> {
            json.v1Deserialize(response, HTestAbc::class)
        }

        // then
        exception.details.size shouldBe 1
        with(exception.details.first()) {
            type shouldBe 5
            address shouldBe "test-address"
            description shouldBe "test-description"
        }
    }

    private data class HTestAbc(
        val test: String
    )

    private data class HTestSuccess(
        override val value: List<HTestSuccess1>
    ) : RootWrapper<List<HTestSuccess1>>

    private data class HTestSuccess1(
        val success: Id5
    )

    private data class Id5(
        val id: Int
    )

}

