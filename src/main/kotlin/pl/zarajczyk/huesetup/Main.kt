package pl.zarajczyk.huesetup

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import pl.zarajczyk.huesetup.configuration.ConfigurationLoader
import java.io.File

fun main(args: Array<String>) {
    val parser = ArgParser("hue-setup")
    val ip by parser
        .option(ArgType.String, shortName = "i", fullName = "ip", description = "IP of the hue bridge")
        .required()
    val token by parser
        .option(ArgType.String, shortName = "t", fullName = "token", description = "token for the hue bridge")
        .required()
    val definitions by parser
        .option(ArgType.String, shortName = "d", fullName = "definitions", description = "location of file with definitions")
        .required()

    parser.parse(args)

    val config = ConfigurationLoader.load(ip, token, File(definitions))

    val hue = HueSetup(config)
    hue.run()
}