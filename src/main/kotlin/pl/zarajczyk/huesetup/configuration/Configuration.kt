package pl.zarajczyk.huesetup.configuration

data class Bridge(
    val ip: String,
    val token: String
)

data class Definitions(
    val zones: List<ZoneDefinition>,
    val rooms: List<RoomDefinition>,
    val lights: List<LightDefinition>,
    val scenes: List<SceneDefinition>,
    val accessories: List<AccessoryDefinition>,
    val automations: List<AutomationDefinition>
)

data class Configuration(
    val bridge: Bridge,
    val definitions: Definitions
)

// =====================================================================================================================

interface Definition {
    fun uid(): String
}

data class ZoneDefinition(
    override val name: String,
    override val archetype: GroupArchetype
) : GroupDefinition()

data class RoomDefinition(
    override val name: String,
    override val archetype: GroupArchetype
) : GroupDefinition()

abstract class GroupDefinition : Definition {
    abstract val name: String
    abstract val archetype: GroupArchetype

    override fun uid() = name
}

@Suppress("unused")
enum class GroupArchetype {
    living_room, kitchen, dining, bedroom, kids_bedroom, bathroom, nursery, recreation, office, gym, hallway, toilet,
    front_door, garage, terrace, garden, driveway, carport, home, downstairs, upstairs, top_floor, attic, guest_room,
    staircase, lounge, man_cave, computer, studio, music, tv, reading, closet, storage, laundry_room, balcony, porch,
    barbecue, pool, other;
}


data class LightDefinition(
    val name: String,
    val mac: MacAddress,
    val rooms: List<ConfiguredGroupReference>,
    val zones: List<ConfiguredGroupReference>
) : Definition {
    override fun uid() = mac.toMacAddress()
}


data class SceneLightSetup(
    val brightness: Brightness? = null,
    val colorTemperature: ColorTemperature? = null,
    val turnedOn: Boolean? = null,
    val group: ConfiguredGroupReference? = null
)

data class SceneDefinition(
    val name: String,
    val group: ConfiguredGroupReference,
    val setup: List<SceneLightSetup>
) : Definition {
    override fun uid() = "${group.getGroupName()}/$name"
}

data class AccessoryDefinition(
    val name: String,
    val mac: MacAddress
) : Definition {
    override fun uid() = mac.toMacAddress()
}

sealed interface AutomationDefinition {
    val actions: List<AutomationAction>
}

data class TimeAutomationDefinition(
    val name: String,
    val time: LocalTime,
    override val actions: List<AutomationAction>
) : AutomationDefinition

data class ButtonAutomationDefinition(
    val remote: String,
    val button: String,
    val pressType: String,
    override val actions: List<AutomationAction>
) : AutomationDefinition

data class MotionAutomationDefinition(
    val sensor: String,
    val motion: Boolean,
    override val actions: List<AutomationAction>,
    val delay: Timeout,
) : AutomationDefinition

sealed interface AutomationAction {
    val conditions: List<AutomationActionCondition>

    fun description(): String
}

data class WaitAutomationAction(
    val duration: Timeout,
    val actions: List<AutomationAction>,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = "wait $duration"
}

data class DisableSensorAction(
    val sensor: String,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = sensor
}

data class EnableSensorAction(
    val sensor: String,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = sensor
}


data class TurnOnAutomationAction(
    val group: ConfiguredGroupReference,
    val transitionTime: TransitionTime,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = group.getGroupName()
}

data class TurnOffAutomationAction(
    val group: ConfiguredGroupReference,
    val transitionTime: TransitionTime,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = group.getGroupName()
}

data class ColorTemperatureTransitionAutomationAction(
    val group: ConfiguredGroupReference,
    val colorTemperature: ColorTemperature,
    val transitionTime: TransitionTime,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = group.getGroupName()
}

data class SceneAutomationAction(
    val group: ConfiguredGroupReference,
    val scene: String,
    val transitionTime: TransitionTime,
    override val conditions: List<AutomationActionCondition>
) : AutomationAction {
    override fun description() = group.getGroupName()
}

sealed interface AutomationActionCondition

data class AnyOnCondition(
    val group: ConfiguredGroupReference
) : AutomationActionCondition

data class AllOffCondition(
    val group: ConfiguredGroupReference
) : AutomationActionCondition

data class TimeOfDayActionCondition(
    val time: LocalTimePeriod
) : AutomationActionCondition