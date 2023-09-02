package pl.zarajczyk.huesetup.hue.httpclient

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class JsonTest {

    private val json = Json()

    @Test
    fun `should parse object`() {
        // given
        val input = """ { "test": "abc" } """

        // when
        val output = json.deserialize<TestAbc>(input)

        // then
        output.test shouldBe "abc"
    }

    @Test
    fun `should try parse object`() {
        // expect
        json.tryDeserialize<TestAbc>(""" { "test": "abc" } """) shouldNotBe null

        // and
        json.tryDeserialize<XyzAbc>(""" { "test": "abc" } """) shouldBe null

        // and
        json.tryDeserialize<String>(""" { "test": "abc" } """) shouldBe null
    }

    @Test
    fun `should parse wrapper object`() {
        // given
        val response = """ { "test": "abc" } """

        // when
        val output = json.deserialize<TestAbcWrapper>(response)

        // then
        output.value.test shouldBe "abc"
    }

    @Test
    fun `should parse list`() {
        // given
        val response = """ [ { "test": "abc" } ] """

        // when
        val output = json.deserialize<ListTestAbcWrapper>(response)

        // then
        output.value.size shouldBe 1
        output.value.first().test shouldBe "abc"
    }

    @Test
    fun `should parse map`() {
        // given
        val response = """ { "x": { "test": "abc" } } """

        // when
        val output = json.deserialize<MapTestAbcWrapper>(response)

        // then
        output.value.size shouldBe 1
        output.value["x"]?.test shouldBe "abc"
    }

}

private data class TestAbc(
    val test: String
)
private data class XyzAbc(
    val xyz: String
)

private data class TestAbcWrapper(
    override val value: TestAbc
) : RootWrapper<TestAbc>

private data class ListTestAbcWrapper(
    override val value: List<TestAbc>
) : RootWrapper<List<TestAbc>>

private data class MapTestAbcWrapper(
    override val value: Map<String, TestAbc>
) : RootWrapper<Map<String, TestAbc>>