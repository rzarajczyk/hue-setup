package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.AccessoryDefinition
import pl.zarajczyk.huesetup.hue.apiclient.*
import pl.zarajczyk.huesetup.hue.systemclient.HueSystemClient

class AccessoriesComparisonApplier(private val hue: HueSystemClient) :
    ComparisonApplier<AccessoryDefinition, AccessoryDevice, AccessoryStatus> {

    override fun begin() {
        hue.refresh()
    }

    override fun getExistingResources() = hue.apiClient.devices.list()
        .filter { it.product_data.model_id in RemoteHelper.listSupportedModelIds() + "SML001" }
        .map { AccessoryDevice(it, hue.zigbeeMac(it)) }
        .map { HueEntityWrapper(it.mac, it) }

    override fun onUnwanted(hueEntityWrapper: HueEntityWrapper<AccessoryDevice>) {
        println("Accessory ≪${hueEntityWrapper.uid}≫ (${hueEntityWrapper.entity.metadata.name}) exists, but is NOT MENTIONED in the config")
    }

    override fun onMissing(definition: AccessoryDefinition) {
        println("Accessory ≪${definition.uid()}≫ (${definition.name}) is MISSING")
    }

    override fun onIncorrect(
        definition: AccessoryDefinition,
        status: AccessoryStatus,
        hueEntityWrapper: HueEntityWrapper<AccessoryDevice>
    ) {
        print("Accessory ≪${definition.uid()}≫ has incorrect name, expected name: ≪${definition.name}≫")
        System.out.flush()
        hue.apiClient.devices.rename(hueEntityWrapper.entity.id, definition.name)
        println(" - ✔ CORRECTED")
    }

    override fun onCorrect(definition: AccessoryDefinition, hueEntityWrapper: HueEntityWrapper<AccessoryDevice>) {
        println("✔ Accessory ≪${definition.uid()}≫ (${definition.name}) is fine")
    }

    override fun checkCorrectness(
        definition: AccessoryDefinition,
        hueEntityWrapper: HueEntityWrapper<AccessoryDevice>
    ): AccessoryStatus? {
        if (definition.name != hueEntityWrapper.entity.metadata.name) {
            return AccessoryStatus.INCORRECT_NAME
        }
        return null
    }
}

data class AccessoryDevice(private val device: Device, val mac: String) : HueEntity, Resource {
    override val type: RType
        get() = device.type
    override val id: String
        get() = device.id
    override val id_v1: String?
        get() = device.id_v1
    val metadata: DeviceMetadata
        get() = device.metadata
}

enum class AccessoryStatus : Status {
    INCORRECT_NAME
}