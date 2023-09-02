package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.hue.httpclient.ApiVersion
import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient
import pl.zarajczyk.huesetup.hue.httpclient.get

class LightsModule(private val client: HueHttpClient) {

    fun list() = client.get<Lights>("/resource/light", ApiVersion.V2).data

}

data class Lights(
    val data: List<Light>
)

data class Light(
    override val type: RType,
    override val id: String,
    override val id_v1: String?
) : Resource