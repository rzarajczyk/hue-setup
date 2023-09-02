package pl.zarajczyk.huesetup.configuration

import pl.zarajczyk.huesetup.configuration.Brightness.Companion.parseBrightness
import pl.zarajczyk.huesetup.configuration.ColorTemperature.Companion.parseColorTemperature
import pl.zarajczyk.huesetup.configuration.ConfiguredGroupReference.Companion.parseConfiguredGroupReference
import pl.zarajczyk.huesetup.configuration.Timeout.Companion.parseTimeout
import pl.zarajczyk.huesetup.configuration.TransitionTime.Companion.parseTransitionTime

class DefinitionsConverter {

    fun convert(definitions: RawDefinitions): Definitions {
        val gi = ConfigurationIndex(definitions)
        return Definitions(
            zones = definitions.zones.convertZones(),
            rooms = definitions.rooms.convertRooms(),
            lights = definitions.lights.convertLights(gi),
            scenes = definitions.scenes.convertScenes(gi),
            accessories = definitions.accessories.convertAccessories(),
            automations = definitions.automations.convertAutomations(gi)
        )
    }

    private fun List<RawGroupDefinition>.convertRooms() = this.map { raw ->
        RoomDefinition(
            name = raw.name,
            archetype = raw.archetype
        )
    }

    private fun List<RawGroupDefinition>.convertZones() = this.map { raw ->
        ZoneDefinition(
            name = raw.name,
            archetype = raw.archetype
        )
    }

    private fun List<RawLightDefinition>.convertLights(configurationIndex: ConfigurationIndex) = this.flatMap { raw ->
        configurationIndex.validateGroups(raw.group)
        raw.mac.mapIndexed { index, mac ->
            LightDefinition(
                name = if (raw.mac.size == 1) raw.name else "${raw.name} #${index + 1}",
                mac = MacAddress(mac),
                rooms = raw.group.filter { configurationIndex.isRoom(it) }.map { it.parseConfiguredGroupReference() },
                zones = raw.group.filter { configurationIndex.isZone(it) }.map { it.parseConfiguredGroupReference() }
            )
        }
    }

    private fun List<RawSceneDefinition>.convertScenes(configurationIndex: ConfigurationIndex) = this.flatMap { raw ->
        configurationIndex.validateGroups(raw.groups)
        raw.groups.map { group ->
            SceneDefinition(
                name = raw.name,
                group = group.parseConfiguredGroupReference(),
                setup = raw.setup.map {
                    configurationIndex.validateGroup(it.group)
                    SceneLightSetup(
                        group = it.group?.parseConfiguredGroupReference(),
                        turnedOn = it.turned_on,
                        brightness = it.brightness?.parseBrightness(),
                        colorTemperature = it.color_temperature?.parseColorTemperature()
                    )
                }
            )
        }
    }

    private fun List<RawAccessoryDefinition>.convertAccessories() = this.map { raw ->
        AccessoryDefinition(
            name = raw.name,
            mac = MacAddress(raw.mac)
        )
    }

    private fun List<RawAutomationDefinition>.convertAutomations(configurationIndex: ConfigurationIndex): List<AutomationDefinition> {
        return this.flatMap { raw ->
            when (raw) {
                is RawTimeAutomationDefinition -> raw.actions.convertActions(configurationIndex).map {
                    TimeAutomationDefinition(
                        name = "${raw.name} ${it.description()}",
                        time = LocalTime(raw.time),
                        actions = listOf(it)
                    )
                }

                is RawButtonAutomationDefinition -> raw.remote.map { remote ->
                    configurationIndex.validateAccessory(remote)
                    ButtonAutomationDefinition(
                        remote = remote,
                        button = raw.button,
                        pressType = raw.pressType,
                        actions = raw.actions.convertActions(configurationIndex, remote)
                    )
                }

                is RawMotionAutomationDefinition -> {
                    configurationIndex.validateAccessory(raw.sensor)
                    listOf(
                        MotionAutomationDefinition(
                            sensor = raw.sensor,
                            motion = raw.motion,
                            actions = raw.actions.convertActions(configurationIndex)
                        )
                    )
                }
            }
        }
    }

    private fun calculateGroups(
        groups: List<String>?,
        groupsPerRemote: Map<String, List<String>>?,
        remoteName: String?,
        configurationIndex: ConfigurationIndex
    ): List<ConfiguredGroupReference> {
        if (groups == null && groupsPerRemote == null) {
            throw RuntimeException("Please specify one of: groups, groupsPerRemote")
        }
        if (groups != null && groupsPerRemote != null) {
            throw RuntimeException("Please specify only one: groups, groupsPerRemote")
        }

        val groups = when {
            groupsPerRemote != null -> {
                if (remoteName == null) {
                    throw RuntimeException("groupsPerRemote can be used only for remote-related automations")
                }
                groupsPerRemote[remoteName]
                    ?: throw RuntimeException("groupsPerRemote not specified for remote ≪$remoteName≫")
            }

            groups != null -> groups
            else -> throw RuntimeException("This should not happen")
        }

        configurationIndex.validateGroups(groups)
        return groups.map { it.parseConfiguredGroupReference() }
    }

    private fun List<RawAutomationAction>.convertActions(
        configurationIndex: ConfigurationIndex,
        remoteName: String? = null
    ): List<AutomationAction> = this.flatMap { raw ->
        when (raw) {
            is RawTurnOnAutomationAction -> calculateGroups(
                groups = raw.group,
                groupsPerRemote = raw.groupPerRemote,
                remoteName = remoteName,
                configurationIndex = configurationIndex
            )
                .map {
                    TurnOnAutomationAction(
                        group = it,
                        conditions = raw.conditions.parseConditions(configurationIndex, it),
                        transitionTime = raw.transitionTime.parseTransitionTime(),
                    )
                }

            is RawTurnOffAutomationAction -> calculateGroups(
                groups = raw.group,
                groupsPerRemote = raw.groupPerRemote,
                remoteName = remoteName,
                configurationIndex = configurationIndex
            )
                .map {
                    TurnOffAutomationAction(
                        group = it,
                        conditions = raw.conditions.parseConditions(configurationIndex, it),
                        transitionTime = raw.transitionTime.parseTransitionTime(),
                    )
                }

            is RawColorTemperatureTransitionAutomationAction -> calculateGroups(
                groups = raw.group,
                groupsPerRemote = raw.groupPerRemote,
                remoteName = remoteName,
                configurationIndex = configurationIndex
            )
                .map {
                    ColorTemperatureTransitionAutomationAction(
                        group = it,
                        colorTemperature = raw.colorTemperature.parseColorTemperature(),
                        transitionTime = raw.transitionTime.parseTransitionTime(),
                        conditions = raw.conditions.parseConditions(configurationIndex, it)
                    )
                }

            is RawSceneAutomationAction -> calculateGroups(
                groups = raw.group,
                groupsPerRemote = raw.groupPerRemote,
                remoteName = remoteName,
                configurationIndex = configurationIndex
            )
                .map {
                    SceneAutomationAction(
                        group = it,
                        scene = raw.scene,
                        transitionTime = raw.transitionTime.parseTransitionTime(),
                        conditions = raw.conditions.parseConditions(configurationIndex, it)
                    )
                }

            is RawDisableSensorAction -> listOf(
                DisableSensorAction(
                    sensor = raw.sensor,
                    conditions = raw.conditions.parseConditions(configurationIndex)
                )
            )

            is RawEnableSensorAction -> listOf(
                EnableSensorAction(
                    sensor = raw.sensor,
                    conditions = raw.conditions.parseConditions(configurationIndex)
                )
            )

            is RawWaitAutomationAction -> listOf(
                WaitAutomationAction(
                    duration = raw.duration.parseTimeout(),
                    actions = raw.actions.convertActions(configurationIndex, remoteName),
                    conditions = raw.conditions.parseConditions(configurationIndex)
                )
            )
        }
    }

    private fun List<RawAutomationActionCondition>.parseConditions(
        configurationIndex: ConfigurationIndex,
        group: ConfiguredGroupReference? = null,
    ) =
        this.map { raw ->
            when (raw) {
                is RawAnyOnCondition -> AnyOnCondition(conditionGroup(raw.group, group, configurationIndex))
                is RawAllOffCondition -> AllOffCondition(conditionGroup(raw.group, group, configurationIndex))
                is RawTimeOfDayActionCondition -> TimeOfDayActionCondition(LocalTimePeriod(raw.time))
            }
        }

    private fun conditionGroup(
        selfGroup: String?,
        parentGroup: ConfiguredGroupReference?,
        configurationIndex: ConfigurationIndex
    ): ConfiguredGroupReference {
        configurationIndex.validateGroup(selfGroup)
        return selfGroup
            ?.parseConfiguredGroupReference()
            ?: parentGroup
            ?: throw RuntimeException("condition group is not set")
    }

}