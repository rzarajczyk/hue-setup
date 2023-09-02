package pl.zarajczyk.huesetup.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import pl.zarajczyk.huesetup.configuration.Brightness.Companion.BRIGHTNESS_REGEXP
import pl.zarajczyk.huesetup.configuration.ColorTemperature.Companion.COLOR_TEMPERATURE_REGEXP
import pl.zarajczyk.huesetup.configuration.LocalTime.Companion.TIME_REGEXP
import pl.zarajczyk.huesetup.configuration.LocalTimePeriod.Companion.TIME_PERIOD_REGEXP
import pl.zarajczyk.huesetup.configuration.MacAddress.Companion.MAC_REGEXP
import pl.zarajczyk.huesetup.configuration.Timeout.Companion.TIMEOUT_REGEXP
import pl.zarajczyk.huesetup.configuration.TransitionTime.Companion.TRANSITION_TIME_REGEXP


data class RawDefinitions(
    @field:Valid
    val zones: List<RawGroupDefinition>,
    @field:Valid
    val rooms: List<RawGroupDefinition>,
    @field:Valid
    val lights: List<RawLightDefinition>,
    @field:Valid
    val scenes: List<RawSceneDefinition>,
    @field:Valid
    val accessories: List<RawAccessoryDefinition>,
    @field:Valid
    val automations: List<RawAutomationDefinition>
)

data class RawGroupDefinition(
    @field:Size(max = 32)
    val name: String,
    @JsonProperty("class")
    val archetype: GroupArchetype
)

data class RawLightDefinition(
    @field:Size(max = 20)
    val name: String,
    val mac: List<@Pattern(regexp = MAC_REGEXP) String>,
    val group: List<String> = emptyList()
)

data class RawAccessoryDefinition(
    @field:Size(max = 32)
    val name: String,
    @Pattern(regexp = MAC_REGEXP)
    val mac: String
)

data class RawSceneDefinition(
    @field:Size(max = 32)
    val name: String,
    val groups: List<String>,
    val setup: List<RawSceneLightSetup>,
)

data class RawSceneLightSetup(
    @field:Pattern(regexp = BRIGHTNESS_REGEXP)
    val brightness: String? = null,
    @JsonProperty("color-temperature")
    @field:Pattern(regexp = COLOR_TEMPERATURE_REGEXP)
    val color_temperature: String? = null,
    @JsonProperty("turned-on")
    val turned_on: Boolean? = null,
    val group: String? = null
)

// =====================================================================================================================

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = RawTimeAutomationDefinition::class, name = "time"),
        JsonSubTypes.Type(value = RawButtonAutomationDefinition::class, name = "button"),
        JsonSubTypes.Type(value = RawMotionAutomationDefinition::class, name = "motion")
    ]
)
sealed interface RawAutomationDefinition

data class RawTimeAutomationDefinition(
    @field:Size(max = 12)
    val name: String,
    @field:Pattern(regexp = TIME_REGEXP)
    val time: String,
    @field:Valid
    val actions: List<@Valid RawAutomationAction>
) : RawAutomationDefinition

data class RawButtonAutomationDefinition(
    val remote: List<String>,
    val button: String,
    @JsonProperty("press-type")
    val pressType: String,
    @field:Valid
    val actions: List<@Valid RawAutomationAction>
) : RawAutomationDefinition

data class RawMotionAutomationDefinition(
    val sensor: String,
    val motion: Boolean,
    @field:Valid
    val actions: List<@Valid RawAutomationAction>
) : RawAutomationDefinition

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = RawTurnOnAutomationAction::class, name = "turn_on"),
        JsonSubTypes.Type(value = RawTurnOffAutomationAction::class, name = "turn_off"),
        JsonSubTypes.Type(value = RawColorTemperatureTransitionAutomationAction::class, name = "color_temperature"),
        JsonSubTypes.Type(value = RawSceneAutomationAction::class, name = "scene"),
        JsonSubTypes.Type(value = RawEnableSensorAction::class, name = "enable_sensor"),
        JsonSubTypes.Type(value = RawDisableSensorAction::class, name = "disable_sensor"),
        JsonSubTypes.Type(value = RawWaitAutomationAction::class, name = "wait"),
    ]
)
sealed interface RawAutomationAction {
    val conditions: List<@Valid RawAutomationActionCondition>
}

data class RawWaitAutomationAction(
    @field:Pattern(regexp = TIMEOUT_REGEXP)
    val duration: String,
    @field:Valid
    val actions: List<@Valid RawAutomationAction>,
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction

data class RawDisableSensorAction(
    val sensor: String,
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction

data class RawEnableSensorAction(
    val sensor: String,
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction

data class RawTurnOnAutomationAction(
    val group: List<String>?,
    @JsonProperty("group-per-remote")
    val groupPerRemote: Map<String, List<String>>?,
    @JsonProperty("transition-time")
    @field:Pattern(regexp =  TRANSITION_TIME_REGEXP)
    val transitionTime: String = "2ds",
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction
data class RawTurnOffAutomationAction(
    val group: List<String>?,
    @JsonProperty("group-per-remote")
    val groupPerRemote: Map<String, List<String>>?,
    @JsonProperty("transition-time")
    @field:Pattern(regexp =  TRANSITION_TIME_REGEXP)
    val transitionTime: String = "2ds",
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction
data class RawColorTemperatureTransitionAutomationAction(
    val group: List<String>?,
    @JsonProperty("group-per-remote")
    val groupPerRemote: Map<String, List<String>>?,
    @JsonProperty("color-temperature")
    @field:Pattern(regexp = COLOR_TEMPERATURE_REGEXP)
    val colorTemperature: String,
    @JsonProperty("transition-time")
    @field:Pattern(regexp =  TRANSITION_TIME_REGEXP)
    val transitionTime: String = "3ds",
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction

data class RawSceneAutomationAction(
    val group: List<String>?,
    @JsonProperty("group-per-remote")
    val groupPerRemote: Map<String, List<String>>?,
    val scene: String,
    @field:Pattern(regexp = TRANSITION_TIME_REGEXP)
    @JsonProperty("transition-time")
    val transitionTime: String = "3ds",
    @field:Valid
    override val conditions: List<RawAutomationActionCondition> = emptyList()
) : RawAutomationAction

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = RawAnyOnCondition::class, name = "any_on"),
        JsonSubTypes.Type(value = RawAllOffCondition::class, name = "all_off"),
        JsonSubTypes.Type(value = RawTimeOfDayActionCondition::class, name = "time_of_day"),
    ]
)
sealed interface RawAutomationActionCondition

data class RawAnyOnCondition(val group: String?) : RawAutomationActionCondition
data class RawAllOffCondition(val group: String?) : RawAutomationActionCondition
data class RawTimeOfDayActionCondition(
    @field:Pattern(regexp = TIME_PERIOD_REGEXP)
    val time: String
) : RawAutomationActionCondition