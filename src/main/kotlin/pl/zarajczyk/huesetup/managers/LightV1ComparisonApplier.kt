package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.ConfiguredGroupReference
import pl.zarajczyk.huesetup.configuration.ConfiguredGroupReference.Companion.parseConfiguredGroupReference
import pl.zarajczyk.huesetup.configuration.LightDefinition
import pl.zarajczyk.huesetup.hue.apiclient.*
import pl.zarajczyk.huesetup.hue.systemclient.HueSystemClient

class LightV1ComparisonApplier(private val hue: HueSystemClient) :
    ComparisonApplier<LightDefinition, LightV1, LightStatus> {

    private lateinit var lights: List<Light>

    override fun begin() {
        lights = hue.apiClient.lights.list()
        hue.refresh()
    }

    override fun getExistingResources() = hue.apiClient.v1_lights.list().values.map { HueEntityWrapper(it.mac(), it) }

    override fun onUnwanted(hueEntityWrapper: HueEntityWrapper<LightV1>) {
        println("Light ≪${hueEntityWrapper.uid}≫ (${hueEntityWrapper.entity.name}) exists, but is NOT MENTIONED in the config")
    }

    override fun onMissing(definition: LightDefinition) {
        println("Light ≪${definition.uid()}≫ is MISSING")
    }

    override fun onIncorrect(
        definition: LightDefinition,
        status: LightStatus,
        hueEntityWrapper: HueEntityWrapper<LightV1>
    ) {
        when (status) {
            is IncorrectName -> {
                val expectedName = status.expectedName
                print("Light ≪${definition.uid()}≫ (${definition.name}) is incorrect - expected name ≪$expectedName≫, actual ≪${hueEntityWrapper.entity.name}≫")
                System.out.flush()
                hue.apiClient.v1_lights.rename(hueEntityWrapper.entity.id, expectedName)
                println(" - ✔ CORRECTED")
            }

            is IncorrectGroup -> {
                print("Light ${definition.uid()} (${definition.name}) is incorrect - $status")
                System.out.flush()
                val hueLightId = hueEntityWrapper.entity.id
                val lightV2 = hue.findLightByV1Id(hueLightId)
                val deviceV2 = hue.findDeviceByServiceReference(lightV2.asReference())
                status.unwantedInRooms
                    .map { name -> hue.findRoom(name) }
                    .forEach { hue.apiClient.rooms.removeChild(it, deviceV2.asReference()) }
                status.missingInRooms
                    .map { name -> hue.findRoom(name) }
                    .forEach { hue.apiClient.rooms.addChild(it, deviceV2.asReference()) }
                status.unwantedInZones
                    .map { name -> hue.findZone(name) }
                    .forEach { hue.apiClient.zones.removeChild(it, lightV2.asReference()) }
                status.missingInZones
                    .map { name -> hue.findZone(name) }
                    .forEach { hue.apiClient.zones.addChild(it, lightV2.asReference()) }
                println(" - ✔ CORRECTED")
            }
        }

    }

    override fun onCorrect(definition: LightDefinition, hueEntityWrapper: HueEntityWrapper<LightV1>) {
        println("✔ Light ≪${definition.uid()}≫ (${definition.name}) is fine")
    }

    override fun checkCorrectness(
        definition: LightDefinition,
        hueEntityWrapper: HueEntityWrapper<LightV1>
    ): LightStatus? {
        val expectedName = getExpectedName(definition.name, hueEntityWrapper.entity)
        if (expectedName != hueEntityWrapper.entity.name) {
            return IncorrectName(expectedName)
        }

        compareGroups(hueEntityWrapper.entity, definition.rooms, definition.zones)?.let {
            return it
        }

        return null
    }

    private fun compareGroups(
        light: LightV1,
        expectedRooms: Collection<ConfiguredGroupReference>,
        expectedZones: Collection<ConfiguredGroupReference>
    ): IncorrectGroup? {
        val hueLightId = light.id
        val lightV2 = hue.findLightByV1Id(hueLightId)
        val deviceV2 = hue.findDeviceByServiceReference(lightV2.asReference())
        val roomsContaining = hue.rooms()
            .filter { deviceV2.asReference() in it.children }
            .map { it.metadata.name.parseConfiguredGroupReference() }
            .toSet()
        val zonesContaining = hue.zones()
            .filter { lightV2.asReference() in it.children }
            .map { it.metadata.name.parseConfiguredGroupReference() }
            .toSet()

        return IncorrectGroup(
            unwantedInRooms = roomsContaining - expectedRooms.toSet(),
            missingInRooms = expectedRooms - roomsContaining.toSet(),
            unwantedInZones = zonesContaining - expectedZones.toSet(),
            missingInZones = expectedZones - zonesContaining.toSet(),
        ).nullIfEmpty()
    }

    private fun getExpectedName(name: String, light: LightV1): String {
        val type = light.parseLightType()
        val maxlumen = light.capabilities.control.maxlumen
        return when (type) {
            LightV1Type.SMART_PLUG -> name
            LightV1Type.COLOR -> "$name /color $maxlumen"
            LightV1Type.WHITE_AMBIANCE -> "$name /amb $maxlumen"
            LightV1Type.WHITE -> "$name /white $maxlumen"
            LightV1Type.OTHER -> name
        }
    }
}

sealed interface LightStatus : Status

data class IncorrectName(val expectedName: String) : LightStatus
data class IncorrectGroup(
    val unwantedInRooms: Collection<ConfiguredGroupReference>,
    val missingInRooms: Collection<ConfiguredGroupReference>,
    val unwantedInZones: Collection<ConfiguredGroupReference>,
    val missingInZones: Collection<ConfiguredGroupReference>,
) : LightStatus {
    fun nullIfEmpty() = if (
        unwantedInRooms.isNotEmpty() ||
        missingInRooms.isNotEmpty() ||
        unwantedInZones.isNotEmpty() ||
        missingInZones.isNotEmpty()
    ) this else null
}