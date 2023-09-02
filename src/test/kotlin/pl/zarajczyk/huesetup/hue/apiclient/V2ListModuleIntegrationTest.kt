//package pl.zarajczyk.huesetup.hue.apiclient
//
//import io.kotest.matchers.ints.shouldBeGreaterThan
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.Arguments.arguments
//import org.junit.jupiter.params.provider.MethodSource
//import pl.zarajczyk.huesetup.hue.AbstractIntegrationTest
//
//
//class V2ListModuleIntegrationTest : AbstractIntegrationTest() {
//
//    @ParameterizedTest
//    @MethodSource("list")
//    fun `should list all resources`(module: V2List<*>) {
//        // when
//        val list = module.list()
//
//        // then
//        list.size shouldBeGreaterThan 0
//    }
//
//    companion object {
//        @JvmStatic
//        fun list() = modules()
//            .filterIsInstance<V2List<*>>()
//            .map { arguments(it) }
//
//    }
//}