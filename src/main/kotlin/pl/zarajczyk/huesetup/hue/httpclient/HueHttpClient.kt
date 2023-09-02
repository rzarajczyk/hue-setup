package pl.zarajczyk.huesetup.hue.httpclient

import pl.zarajczyk.huesetup.hue.httpclient.Method.*
import java.net.URI
import kotlin.reflect.KClass

enum class Method { GET, POST, PUT, DELETE }
enum class ApiVersion { V1, V2 }

class HueHttpClient(val ip: String, val token: String) {

    private val json = HueJson()

    fun <T : Any> v1(method: Method, resource: String, klass: KClass<T>, reqBody: Any? = null): T = try {
        val response = Http.request(
            url = URI.create("https://$ip/api/$token$resource").toURL(),
            body = reqBody?.let { json.serialize(it) },
            method = method.name
        )
        json.v1Deserialize(response, klass)
    } catch (e: Exception) {
        println("${method.name} ${URI.create("https://$ip/api/$token$resource").toURL()}\n\n${reqBody?.let { json.serialize(it) }}")
        throw e
    }

    fun <T : Any> v2(method: Method, resource: String, klass: KClass<T>, reqBody: Any? = null): T {
        val response = Http.request(
            url = URI.create("https://$ip/clip/v2$resource").toURL(),
            body = reqBody?.let { json.serialize(it) },
            method = method.name,
            headers = mapOf("hue-application-key" to token)
        )
        val result = json.v2Deserialize(response, klass)
        if (method != GET) {
            Thread.sleep(50) // do not flood bridge with the requests
        }
        return result
    }
}

inline fun <reified T : Any> HueHttpClient.get(resource: String, version: ApiVersion = ApiVersion.V2) =
    when (version) {
        ApiVersion.V1 -> v1(GET, resource, T::class)
        ApiVersion.V2 -> v2(GET, resource, T::class)
    }

inline fun <reified T : Any> HueHttpClient.post(resource: String, body: Any, version: ApiVersion = ApiVersion.V2) =
    when (version) {
        ApiVersion.V1 -> v1(POST, resource, T::class, body)
        ApiVersion.V2 -> v2(POST, resource, T::class, body)
    }

inline fun <reified T : Any> HueHttpClient.delete(resource: String, version: ApiVersion = ApiVersion.V2) =
    when (version) {
        ApiVersion.V1 -> v1(DELETE, resource, T::class)
        ApiVersion.V2 -> v2(DELETE, resource, T::class)
    }

inline fun <reified T : Any> HueHttpClient.put(resource: String, body: Any, version: ApiVersion = ApiVersion.V2) =
    when (version) {
        ApiVersion.V1 -> v1(PUT, resource, T::class, body)
        ApiVersion.V2 -> v2(PUT, resource, T::class, body)
    }