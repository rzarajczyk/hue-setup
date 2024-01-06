//package pl.zarajczyk.huesetup.hue.httpclient
//
//import com.fasterxml.jackson.databind.JsonNode
//import pl.zarajczyk.huesetup.hue.AbstractIntegrationTest
//import kotlin.test.Test
//
//class HueHttpClientTest : AbstractIntegrationTest() {
//
//    @Test
//    fun `should be able to call hue using v1 api`() {
//        // when
//        val result = hueHttpClient.v1(Method.GET, "/", JsonNode::class, null)
//
//        // then
//        println(result)
//    }
//
//}