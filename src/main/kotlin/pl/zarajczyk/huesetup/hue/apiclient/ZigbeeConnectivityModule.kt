package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.hue.httpclient.ApiVersion
import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient
import pl.zarajczyk.huesetup.hue.httpclient.get

class ZigbeeConnectivityModule(private val client: HueHttpClient) {

    fun list() = client.get<ZigbeeConnectivities>("/resource/zigbee_connectivity", ApiVersion.V2).data

}

data class ZigbeeConnectivities(
    val data: List<ZigbeeConnectivity>
)

data class ZigbeeConnectivity(
    override val id: String,
    override val id_v1: String?,
    override val type: RType,
    val owner: Reference,
    val status: ZigbeeConnectivityStatus,
    val mac_address: String,
    val channel: ZigbeeConnectivityChannel?
) : HueEntity, Resource


@Suppress("unused")
enum class ZigbeeConnectivityStatus {
    connected, disconnected, connectivity_issue, unidirectional_incoming
}

data class ZigbeeConnectivityChannel(
    val status: ZigbeeConnectivityChannelStatus,
    val value: ZigbeeConnectivityChannelValue
)

@Suppress("unused")
enum class ZigbeeConnectivityChannelStatus { set, changing }

@Suppress("unused")
enum class ZigbeeConnectivityChannelValue { channel_11, channel_15, channel_20, channel_25, not_configured }