//package pl.zarajczyk.huesetup.hue.apiclient
//
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.shouldNotBe
//import org.junit.jupiter.api.Test
//import pl.zarajczyk.huesetup.hue.AbstractIntegrationTest
//
//class LightsV1V1ModuleIntegrationTest : AbstractIntegrationTest() {
//
//    @Test
//    fun `should return something`() {
//        // expect
//        hue.v1_lights.list() shouldNotBe emptyMap<String, LightV1>()
//    }
//
//    @Test
//    fun `should rename light`() {
//        // given
//        val light = hue.v1_lights.list().entries.first().value
//        val originalName = light.name
//
//        // when
//        hue.v1_lights.update(light.id, LightV1Update("_${originalName}_"))
//        val newName = hue.v1_lights.get(light.id).name
//
//        // then
//        newName shouldBe "_${originalName}_"
//
//        // cleanup
//        hue.v1_lights.update(light.id, LightV1Update(originalName))
//    }
//
//}