package pl.zarajczyk.huesetup.hue.httpclient

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.reflect.KClass

class Json(
    private val om: ObjectMapper
) {

    constructor() : this(ObjectMapper().registerKotlinModule())

    fun serialize(v: Any): String = om.writeValueAsString(v)

    fun read(v: String): JsonNode = om.readTree(v)

    fun <T : Any> deserialize(v: String, klass: KClass<T>): T =
        if (RootWrapper::class.java.isAssignableFrom(klass.java)) {
            om.readValue("""{"value": $v}""", klass.java)
        } else {
            om.readValue(v, klass.java)
        }

    fun <T : Any> tryDeserialize(v: String, klass: KClass<T>) = tryDeserialize(klass) {
        deserialize(v, klass)
    }

    fun <T : Any> deserialize(v: JsonNode, klass: KClass<T>): T =
        if (RootWrapper::class.java.isAssignableFrom(klass.java)) {
            val wrapper = om.createObjectNode()
            wrapper.set<JsonNode>("value", v)
            om.convertValue(wrapper, klass.java)
        } else {
            om.convertValue(v, klass.java)
        }

    fun <T : Any> tryDeserialize(v: JsonNode, klass: KClass<T>) = tryDeserialize(klass) {
        deserialize(v, klass)
    }

    private fun <T : Any> tryDeserialize(klass: KClass<T>, code: () -> T) = try {
        code()
    } catch (e: IllegalArgumentException) {
        null
    } catch (e: JsonMappingException) {
        null
    }
}

interface RootWrapper<T : Any> {
    val value: T
}

inline fun <reified T : Any> Json.deserialize(v: String) =
    this.deserialize(v, T::class)

inline fun <reified T : Any> Json.deserialize(v: JsonNode) =
    this.deserialize(v, T::class)

inline fun <reified T : Any> Json.tryDeserialize(v: String) =
    this.tryDeserialize(v, T::class)

inline fun <reified T : Any> Json.tryDeserialize(v: JsonNode) =
    this.tryDeserialize(v, T::class)