package pl.zarajczyk.huesetup

import pl.zarajczyk.huesetup.configuration.Configuration
import pl.zarajczyk.huesetup.hue.apiclient.HueApiClient
import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient
import pl.zarajczyk.huesetup.hue.systemclient.HueSystemClient
import pl.zarajczyk.huesetup.managers.*

class HueSetup(configuration: Configuration) {

    private val hue = HueSystemClient(
        HueApiClient(
            HueHttpClient(configuration.bridge.ip, configuration.bridge.token)
        )
    )

    private val managers: List<Manager> = listOf(
        ComparingManager(configuration.definitions.rooms, GroupComparisonApplier(hue.apiClient.rooms)),
        ComparingManager(configuration.definitions.zones, GroupComparisonApplier(hue.apiClient.zones)),
        ComparingManager(configuration.definitions.lights, LightV1ComparisonApplier(hue)),
        ComparingManager(configuration.definitions.scenes, SceneComparisonApplier(hue)),
        ComparingManager(configuration.definitions.accessories, AccessoriesComparisonApplier(hue)),
//        AutomationsManager(configuration.definitions.automations, hue)
    )

    fun run() {
        managers.forEach {
            it.run()
        }
    }

}