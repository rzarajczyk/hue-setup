package pl.zarajczyk.huesetup.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.validation.Validation
import org.apache.commons.text.StringSubstitutor
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator

object ConfigurationLoader {

    fun load(): Configuration {
        val om = ObjectMapper(YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .registerKotlinModule()
        val bridge = om.readValue<Bridge>(resource("/bridge.yaml"))
        val definitions = om.readValue<RawDefinitions>(resource("/definitions.yaml"))
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
            bridge = bridge,
            definitions = DefinitionsConverter().convert(definitions)
        )
    }

    private fun resource(path: String): String {
        val text = ConfigurationLoader::class.java
            .getResourceAsStream(path)
            ?.bufferedReader()
            .use { it?.readText() }
            ?: throw RuntimeException("Resource not found ≪$path≫")
        return StringSubstitutor(System.getenv()).replace(text)
    }

}