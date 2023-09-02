package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.Definition
import pl.zarajczyk.huesetup.hue.apiclient.HueEntity

class ComparingManager<D : Definition, T : HueEntity, R : Status>(
    private val definitions: List<D>,
    private val comparisonApplier: ComparisonApplier<D, T, R>,
) : Manager {

    override fun run() {
        run(definitions, comparisonApplier)
    }

    private fun run(definitions: List<D>, comparisonApplier: ComparisonApplier<D, T, R>) {
        comparisonApplier.begin()
        val existingResources = comparisonApplier.getExistingResources().associateBy { it.uid }
        definitions.forEach { definition ->
            if (definition.uid() in existingResources) {
                val resource = existingResources.getValue(definition.uid())
                val status = comparisonApplier.checkCorrectness(definition, resource)
                if (status == null) {
                    comparisonApplier.onCorrect(definition, resource)
                } else {
                    comparisonApplier.onIncorrect(definition, status, resource)
                }
            } else {
                comparisonApplier.onMissing(definition)
            }
        }
        existingResources
            .filterKeys { uid -> uid !in definitions.map { it.uid() } }
            .values
            .forEach { zone -> comparisonApplier.onUnwanted(zone) }
        comparisonApplier.end()
    }

}

interface Status