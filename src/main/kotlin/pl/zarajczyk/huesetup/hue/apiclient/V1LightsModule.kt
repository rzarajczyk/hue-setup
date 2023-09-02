package pl.zarajczyk.huesetup.hue.apiclient

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import pl.zarajczyk.huesetup.hue.httpclient.*
import java.time.Instant

class V1LightsModule(private val client: HueHttpClient) {

    fun list() = client.get<LightsV1>("/lights", ApiVersion.V1).value.mapValues { (k, v) -> v.copy(id = k) }

    fun get(id: String) = client.get<LightV1>("/lights/$id", ApiVersion.V1).copy(id = id)

    private fun update(id: String, light: LightV1Update) = client.put<LightV1UpdateResponse>("/lights/$id", light, ApiVersion.V1)

    fun rename(id: String, name: String) = update(id, LightV1Update(name))

}

data class LightV1Update(
    val name: String
)

data class LightV1UpdateResponse(
    override val value: List<LightV1UpdateSuccess>
) : RootWrapper<List<LightV1UpdateSuccess>>

data class LightV1UpdateSuccess(
    val success: Map<String,String>
)

data class LightsV1(
    override val value: Map<String, LightV1>
) : RootWrapper<Map<String, LightV1>>

data class LightV1(
    @JsonIgnore
    val id: String = "",
    val name: String,
    val type: String,
    val modelid: String,
    val manufacturername: String,
    val productname: String,
    val uniqueid: String,
    val swversion: String,
    val config: LightV1Config,
    val swupdate: LightV1SwUpdate,
    val capabilities: LightV1Capabilities,
    val state: LightV1State
) : HueEntity {
    fun mac() = uniqueid.substringBefore("-")

    fun parseLightType() = when (type) {
        "Extended color light" -> LightV1Type.COLOR
        "Color temperature light" -> LightV1Type.WHITE_AMBIANCE
        "On/Off plug-in unit" -> LightV1Type.SMART_PLUG
        "Dimmable light" -> LightV1Type.WHITE
        else -> LightV1Type.OTHER
    }
}

enum class LightV1Type { SMART_PLUG, COLOR, WHITE_AMBIANCE, WHITE, OTHER }

data class LightV1State(
    val on: Boolean,
    val bri: Int?,
    val ct: Int?,
    val alert: String,
    val colormode: String?,
    val mode: String,
    val reachable: Boolean
)

data class LightV1Config(
    val archetype: String,
    val function: String,
    val direction: String,
    val startup: LightV1ConfigStartup
)

data class LightV1ConfigStartup(
    val mode: String,
    val configured: Boolean
)

data class LightV1SwUpdate (
    val state: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    val lastinstall: Instant
)

data class LightV1Capabilities(
    val certified: Boolean,
    val control: LightV1CapabilitiesControl,
    val streaming: LightV1CapabilitiesStreaming
)

data class LightV1CapabilitiesControl (
    val mindimlevel: Int?,
    val maxlumen: Int?,
    val colorgamuttype: String?,
    val colorgamut: List<List<Double>>?,
    val ct: LightV1CapabilitiesCt?
)

data class LightV1CapabilitiesCt(
    val min: Int,
    val max: Int
)

data class LightV1CapabilitiesStreaming(
    val renderer: Boolean,
    val proxy: Boolean
)