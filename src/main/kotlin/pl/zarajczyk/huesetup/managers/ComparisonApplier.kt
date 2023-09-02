package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.Definition
import pl.zarajczyk.huesetup.hue.apiclient.HueEntity

interface ComparisonApplier<D : Definition, T : HueEntity, S : Status> {

    fun begin() {}

    fun end() {}

    fun getExistingResources(): List<HueEntityWrapper<T>>

    fun checkCorrectness(definition: D, hueEntityWrapper: HueEntityWrapper<T>): S?

    fun onCorrect(definition: D, hueEntityWrapper: HueEntityWrapper<T>)

    fun onIncorrect(definition: D, status: S, hueEntityWrapper: HueEntityWrapper<T>)

    fun onMissing(definition: D)

    fun onUnwanted(hueEntityWrapper: HueEntityWrapper<T>)

}

data class HueEntityWrapper<T : HueEntity>(
    val uid: String,
    val entity: T
)