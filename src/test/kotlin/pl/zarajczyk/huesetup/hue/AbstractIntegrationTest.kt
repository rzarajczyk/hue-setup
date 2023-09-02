package pl.zarajczyk.huesetup.hue

import pl.zarajczyk.huesetup.hue.apiclient.HueApiClient
import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient

abstract class AbstractIntegrationTest {

    companion object {

        val hueHttpClient = HueHttpClient(
            ip = System.getenv("HUE_IP"),
            token = System.getenv("HUE_TOKEN")
        )

        val hue = HueApiClient(hueHttpClient)

        @JvmStatic
        fun modules() = listOf(hue.zones, hue.scenes, hue.rooms, hue.devices, hue.lights)
    }

}