package pl.zarajczyk.huesetup.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.validation.Validation
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import java.io.File

object ConfigurationLoader {

    fun load(ip: String, token: String, definitionsFile: File): Configuration {
        val om = ObjectMapper(YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .registerKotlinModule()
        val definitions = om.readValue<RawDefinitions>(definitionsFile)
        val violations = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory()
            .validator
            .validate(definitions)
        violations.forEach { println("${it.message}: ≪${it.invalidValue}≫ at ${it.propertyPath}") }
        if (violations.isNotEmpty()) {
            throw RuntimeException("Validation errors found: ${violations.size} violations")
        }
        return Configuration(
            bridge = Bridge(ip, token),
            definitions = DefinitionsConverter().convert(definitions)
        )
    }
}