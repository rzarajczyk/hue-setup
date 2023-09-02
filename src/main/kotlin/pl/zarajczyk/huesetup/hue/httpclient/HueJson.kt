package pl.zarajczyk.huesetup.hue.httpclient

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.reflect.KClass

class HueJson {
    private val json = Json(
        ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(JavaTimeModule())
            .registerKotlinModule()
    )

    fun serialize(v: Any): String = json.serialize(v)

    @Throws(HueErrorResponse::class)
    fun <T : Any> v1Deserialize(v: String, klass: KClass<T>): T = try {
        val parsed = json.read(v)
        json.tryDeserialize<BridgeErrorResponse>(parsed)?.let {
            throw HueErrorResponse(it.value.map { it.error })
        }
        json.deserialize(parsed, klass)
    } catch (e: Exception) {
        println("Exception trying to deserialize JSON: $v")
        throw e
    }

    fun <T : Any> v2Deserialize(v: String, klass: KClass<T>): T = try {
        val parsed = json.read(v)
        json.deserialize(parsed, klass)
    } catch (e: Exception) {
        println("Exception trying to deserialize JSON: $v")
        throw e
    }
}
class HueErrorResponse(val details: List<HueError>) :
    RuntimeException("Hue error: ${details.joinToString(";") { it.toString() }}")

data class HueError(
    val type: Int,
    val address: String,
    val description: String
)

data class BridgeErrorResponse(
    override val value: List<BridgeError>
) : RootWrapper<List<BridgeError>>

data class BridgeError(
    val error: HueError
)