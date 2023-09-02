package pl.zarajczyk.huesetup.hue.apiclient

import com.fasterxml.jackson.annotation.JsonIgnore
import pl.zarajczyk.huesetup.hue.httpclient.*

class V1SensorsModule(private val httpClient: HueHttpClient) {

    private fun create(sensor: SensorV1Update) =
        httpClient.post<SensorV1UpdateResponse>("/sensors", sensor, ApiVersion.V1)

    fun createMemorySensor(name: String) = create(
        SensorV1Update(
            name = name,
            modelid = "Automation Time Event",
            swversion = "1.0",
            type = "CLIPGenericStatus",
            uniqueid = randomString(32),
            manufacturername = "Hue Essentials",
            recycle = false
        )
    ).value
        .first()
        .success["id"]
        ?.let { CreateMemorySensorResponse(it) }
        ?: throw RuntimeException("createMemorySensor returned null sensorId")

    private fun randomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun list() = httpClient
        .get<SensorsV1>("/rules", ApiVersion.V1)
        .value
        .mapValues { (k, v) -> v.copy(id = k) }
        .values
        .toList()

    private fun delete(id: String) = httpClient.delete<V1DeletionResult>("/sensors/$id", ApiVersion.V1)

    fun deleteAllMemorySensors() {
        list()
            .filter { it.type == "CLIPGenericStatus" }
            .forEach { delete(it.id) }
    }

}

data class SensorV1(
    @JsonIgnore
    val id: String = "",
    val type: String
)

data class SensorsV1(
    override val value: Map<String, SensorV1>
) : RootWrapper<Map<String, SensorV1>>

data class SensorV1Update(
    val name: String,
    val modelid: String,
    val swversion: String,
    val type: String,
    val uniqueid: String,
    val manufacturername: String,
    val recycle: Boolean
)

data class CreateMemorySensorResponse(
    val sensorId: String
)

data class SensorV1UpdateResponse(
    override val value: List<SensorV1UpdateSuccess>
) : RootWrapper<List<SensorV1UpdateSuccess>>

data class SensorV1UpdateSuccess(
    val success: Map<String, String>
)