package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient
import pl.zarajczyk.huesetup.hue.httpclient.get
import pl.zarajczyk.huesetup.hue.httpclient.put

class DevicesModule(private val client: HueHttpClient) {

    fun list() = client.get<Devices>("/resource/device").data

    private fun update(id: String, device: DeviceUpdate) =
        client.put<ModificationResponse>("/resource/device/$id", device)

    fun rename(id: String, name: String) = update(id, DeviceUpdate(DeviceUpdateMetadata(name)))
}

data class Devices(
    val data: List<Device>
)

data class Device(
    override val type: RType,
    override val id: String,
    override val id_v1: String?,
    val product_data: DeviceProductData,
    val metadata: DeviceMetadata,
    val services: List<Reference>
) : Resource, HueEntity

data class DeviceProductData(
    val model_id: String,
    val manufacturer_name: String,
    val product_name: String,
    val product_archetype: Archetype,
    val certified: Boolean,
    val software_version: String,
    val hardware_platform_type: String?
)

data class DeviceMetadata(
    val name: String,
    val archetype: Archetype,
)

data class DeviceUpdate(
    val metadata: DeviceUpdateMetadata,
)

data class DeviceUpdateMetadata(
    val name: String?
)
