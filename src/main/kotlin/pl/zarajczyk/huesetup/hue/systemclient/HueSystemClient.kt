package pl.zarajczyk.huesetup.hue.systemclient

import pl.zarajczyk.huesetup.configuration.ConfiguredGroupReference
import pl.zarajczyk.huesetup.hue.apiclient.*

class HueSystemClient(val apiClient: HueApiClient) {

    lateinit var rooms: Map<String, Group>
    lateinit var zones: Map<String, Group>
    lateinit var devices: Map<String, Device>
    lateinit var lights: Map<String, Light>
    lateinit var lightsByV1Id: Map<String, Light>
    lateinit var zigbeeConnectivity: Map<String, ZigbeeConnectivity>
    lateinit var scenes: Map<String, Scene>

    init {
        refresh()
    }

    fun refresh() {
        rooms = apiClient.rooms.list().associateBy { it.id }
        zones = apiClient.zones.list().associateBy { it.id }
        devices = apiClient.devices.list().associateBy { it.id }
        lights = apiClient.lights.list().associateBy { it.id }
        lightsByV1Id = lights.values.filter { it.id_v1 != null }.associateBy { it.id_v1!! }
        zigbeeConnectivity = apiClient.zigbeeConnectivity.list().associateBy { it.id }
        scenes = apiClient.scenes.list().associateBy { it.id }
    }

    fun <T> dereference(ref: Reference): T = when (ref.rtype) {
        RType.room -> rooms[ref.rid]
        RType.zone -> zones[ref.rid]
        RType.device -> devices[ref.rid]
        RType.light -> lights[ref.rid]
        RType.zigbee_connectivity -> zigbeeConnectivity[ref.rid]
        else -> throw RuntimeException("Unsupported defererence $ref")
    }
        ?.let { it as T }
        ?: throw InconsistentState("Unable to dereference ${ref.rtype}")

    fun findLightByV1Id(id: String) = lightsByV1Id["/lights/$id"]
        ?: throw InconsistentState("Missing \"/lights/$id\"")

    fun findDeviceByServiceReference(ref: Reference) = devices.values.find { ref in it.services }
        ?: throw InconsistentState("Missing device with service $ref")

    fun findDeviceByName(name: String) = devices.values.find { it.metadata.name == name }
        ?: throw InconsistentState("Missing device with name $name")

    fun findRoom(ref: ConfiguredGroupReference) = rooms.values.find { it.metadata.name == ref.getGroupName() }
        ?: throw InconsistentState("Missing room with name $ref")

    fun findZone(ref: ConfiguredGroupReference) = zones.values.find { it.metadata.name == ref.getGroupName() }
        ?: throw InconsistentState("Missing room with name $ref")

    fun findGroup(ref: ConfiguredGroupReference) = rooms.values.find { it.metadata.name == ref.getGroupName() }
        ?: zones.values.find { it.metadata.name == ref.getGroupName() }
        ?: throw InconsistentState("Missing zone/room with name ${ref.getGroupName()}")

    fun findScene(ref: ConfiguredGroupReference, name: String): Scene {
        val group = findGroup(ref)
        return scenes.values.find { it.group == group.asReference() && it.metadata.name == name }
            ?: throw InconsistentState("Missinc scene $ref/$name")
    }

    fun findChildrenLightServices(refs: List<Reference>) = refs.mapNotNull { ref ->
        when (ref.rtype) {
            RType.light -> ref
            RType.device -> dereference<Device>(ref).services.find { it.rtype == RType.light }
            else -> null
        }
    }

    fun rooms() = rooms.values

    fun zones() = zones.values

    fun zigbeeMac(device: Device): String {
        val zigbeeRef = device.services.find { it.rtype == RType.zigbee_connectivity }
            ?: throw RuntimeException("Device ${device.metadata.name} doesn't have zigbee_connectivity!")
        return dereference<ZigbeeConnectivity>(zigbeeRef).mac_address
    }

}

class InconsistentState(msg: String) : RuntimeException(msg)