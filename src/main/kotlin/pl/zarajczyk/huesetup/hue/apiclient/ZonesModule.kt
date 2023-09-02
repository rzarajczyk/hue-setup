package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.hue.httpclient.HueHttpClient

class ZonesModule(private val client: HueHttpClient) : GroupsModule(client, RType.zone)