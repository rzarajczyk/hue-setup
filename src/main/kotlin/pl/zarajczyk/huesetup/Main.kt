package pl.zarajczyk.huesetup

import pl.zarajczyk.huesetup.configuration.ConfigurationLoader

fun main() {
    val config = ConfigurationLoader.load()

    val hue = HueSetup(config)
    hue.run()

    System.exit(0)
}