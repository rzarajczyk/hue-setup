package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.GroupDefinition
import pl.zarajczyk.huesetup.hue.apiclient.Group
import pl.zarajczyk.huesetup.hue.apiclient.GroupsModule

class GroupComparisonApplier(private val module: GroupsModule) :
    ComparisonApplier<GroupDefinition, Group, GroupStatus> {

    override fun getExistingResources() = module.list()
        .map { HueEntityWrapper(it.metadata.name, it) }

    private fun desc() = module.type.toString().replaceFirstChar { it.uppercase() }

    override fun onUnwanted(hueEntityWrapper: HueEntityWrapper<Group>) {
        println("${desc()} ≪${hueEntityWrapper.uid}≫ WILL BE DELETED")
        module.delete(hueEntityWrapper.entity.id)
    }

    override fun onMissing(definition: GroupDefinition) {
        println("${desc()} ≪${definition.uid()}≫ IS MISSING and will be created")
        module.create(
            name = definition.name,
            type = module.type,
            archetype = definition.archetype
        )
    }

    override fun onIncorrect(
        definition: GroupDefinition,
        status: GroupStatus,
        hueEntityWrapper: HueEntityWrapper<Group>
    ) {
        println("${desc()} ≪${definition.uid()}≫ is INCORRECT and will be corrected")
        module.changeArchetype(hueEntityWrapper.entity, definition.archetype)
    }

    override fun onCorrect(definition: GroupDefinition, hueEntityWrapper: HueEntityWrapper<Group>) {
        println("✔ ${desc()} ≪${definition.uid()}≫ is fine")
    }

    override fun checkCorrectness(
        definition: GroupDefinition,
        hueEntityWrapper: HueEntityWrapper<Group>
    ) = when {
        hueEntityWrapper.entity.metadata.archetype != definition.archetype -> GroupStatus.INCORRECT_ARCHETYPE
        else -> null
    }
}

enum class GroupStatus : Status {
    INCORRECT_ARCHETYPE
}