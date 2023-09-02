package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.SceneDefinition
import pl.zarajczyk.huesetup.hue.apiclient.*
import pl.zarajczyk.huesetup.hue.systemclient.HueSystemClient

class SceneComparisonApplier(private val hue: HueSystemClient) : ComparisonApplier<SceneDefinition, Scene, SceneStatus> {

    override fun begin() {
        hue.refresh()
    }

    override fun getExistingResources() = hue.apiClient.scenes.list().map {
        val groupName = hue.dereference<Group>(it.group).metadata.name
        val uid = "$groupName/${it.metadata.name}"
        HueEntityWrapper(uid, it)
    }

    override fun onUnwanted(hueEntityWrapper: HueEntityWrapper<Scene>) {
        println("Scene ≪${hueEntityWrapper.uid}≫ exists, but is NOT MENTIONED in the config - will be deleted")
        hue.apiClient.scenes.delete(hueEntityWrapper.entity.id)
    }

    override fun onMissing(definition: SceneDefinition) {
        println("Scene ≪${definition.uid()}≫ is MISSING - it will be created")
        val group = hue.findGroup(definition.group)
        val actions = convertDefinitionToActions(definition)
        hue.apiClient.scenes.create(
            name = definition.name,
            group = group.asReference(),
            actions = actions.toList()
        )
    }

    override fun onIncorrect(
        definition: SceneDefinition,
        status: SceneStatus,
        hueEntityWrapper: HueEntityWrapper<Scene>
    ) {
        when (status) {
            is WrongActions -> {
                print("Scene ${definition.uid()} has incorrect actions - it will be corrected")
                System.out.flush()
                hue.apiClient.scenes.changeActions(hueEntityWrapper.entity, status.expectedActions)
                println(" - ✔ CORRECTED")
            }
        }

    }

    override fun onCorrect(definition: SceneDefinition, hueEntityWrapper: HueEntityWrapper<Scene>) {
        println("✔ Scene ${definition.uid()} is fine")
    }

    override fun checkCorrectness(
        definition: SceneDefinition,
        hueEntityWrapper: HueEntityWrapper<Scene>
    ): SceneStatus? {
        val scene = hueEntityWrapper.entity
        if (scene.actions.any { it.target.rtype != RType.light }) {
            throw RuntimeException("[ERROR] Cannot compare scene which refers to non-light entities: $scene")
        }
        val expectedActions = convertDefinitionToActions(definition)
        return if (scene.actions.toSet() == expectedActions) {
            null
        } else {
            WrongActions(expectedActions.toList())
        }
    }

    private fun convertDefinitionToActions(definition: SceneDefinition): Set<SceneAction> {
        return definition.setup.flatMap { setup ->
            val groupName = setup.group ?: definition.group
            val group = hue.findGroup(groupName)
            val lights = hue.findChildrenLightServices(group.children)
            lights.map {
                SceneAction(
                    target = it,
                    action = Action(
                        on = setup.turnedOn?.let { OnAction(it) } ?: OnAction(true),
                        dimming = setup.brightness?.let { DimmingAction(it.toHueBrightnessPercentage()) },
                        color_temperature = setup.colorTemperature?.let { ColorTemperatureAction(it.tuHueMirek()) }
                    )
                )
            }
        }.toSet()
    }
}

sealed interface SceneStatus : Status

data class WrongActions(
    val expectedActions: List<SceneAction>
) : SceneStatus
