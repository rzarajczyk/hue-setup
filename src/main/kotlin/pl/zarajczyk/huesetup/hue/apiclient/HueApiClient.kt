package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient

class HueApiClient(httpClient: HueHttpClient) {

    val rooms = RoomsModule(httpClient)
    val zones = ZonesModule(httpClient)
    val v1_lights = V1LightsModule(httpClient)
    val lights = LightsModule(httpClient)
    val scenes = ScenesModule(httpClient)
    val devices = DevicesModule(httpClient)
    val zigbeeConnectivity = ZigbeeConnectivityModule(httpClient)
    val v1_sensors = V1SensorsModule(httpClient)
    val v1_schedules = V1SchedulesModule(httpClient)
    val v1_rules = V1RulesModule(httpClient)
}

interface HueEntity

interface Resource {
    val type: RType
    val id: String
    val id_v1: String?

    fun asReference() = Reference(id, type)
}

