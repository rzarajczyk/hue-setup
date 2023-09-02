package pl.zarajczyk.huesetup.hue.apiclient

import com.fasterxml.jackson.annotation.JsonIgnore
import pl.zarajczyk.huesetup.hue.httpclient.*

class V1RulesModule(private val httpClient: HueHttpClient) {

    fun create(rule: V1RuleUpdate) = httpClient.post<Any>("/rules", rule, ApiVersion.V1)

    private fun list() = httpClient
        .get<RulesV1>("/rules", ApiVersion.V1)
        .value
        .mapValues { (k, v) -> v.copy(id = k) }
        .values
        .toList()

    private fun delete(id: String) = httpClient.delete<V1DeletionResult>("/rules/$id", ApiVersion.V1)

    fun deleteAll() {
        list().forEach {
            delete(it.id)
        }
    }

}

data class RuleV1(
    @JsonIgnore
    val id: String = ""
)

data class V1DeletionResult(
    override val value: List<V1SingleDeletionResult>
) : RootWrapper<List<V1SingleDeletionResult>>

data class V1SingleDeletionResult(
    val success: String
)

data class RulesV1(
    override val value: Map<String, RuleV1>
) : RootWrapper<Map<String, RuleV1>>


data class V1RuleUpdate(
    val name: String,
    val conditions: List<V1RuleCondition>,
    val actions: List<V1RuleAction>
)

data class V1RuleCondition(
    val address: String,
    val operator: String, // TODO enum
    val value: String?
)
data class V1RuleAction(
    val address: String,
    val method: String, // TODO enum
    val body: Any // TODO type!
)