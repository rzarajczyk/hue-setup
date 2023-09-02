package pl.zarajczyk.huesetup.hue.apiclient

import com.fasterxml.jackson.annotation.JsonIgnore
import pl.zarajczyk.huesetup.hue.httpclient.*

class V1SchedulesModule(private val httpClient: HueHttpClient) {

    private fun create(schedule: ScheduleV1Update) = httpClient.post<Any>("/schedules", schedule, ApiVersion.V1)

    fun createScheduleForUpdatingSensor(name: String, sensorId: String, localtime: String) = create(
        ScheduleV1Update(
            name = name,
            description = "",
            localtime = localtime,
            command = ScheduleCommandV1(
                address = "/api/${httpClient.token}/sensors/$sensorId/state",
                method = Method.PUT,
                body = ScheduleCommandBodyV1(
                    status = 1
                )
            )
        )
    )

    private fun list() = httpClient
        .get<SchedulesV1>("/schedules", ApiVersion.V1)
        .value
        .mapValues { (k, v) -> v.copy(id = k) }
        .values
        .toList()

    private fun delete(id: String) = httpClient.delete<V1DeletionResult>("/schedules/$id", ApiVersion.V1)

    fun deleteAll() {
        list().forEach {
            delete(it.id)
        }
    }

}

data class ScheduleV1(
    @JsonIgnore
    val id: String= ""
)

data class SchedulesV1(
    override val value: Map<String, ScheduleV1>
) : RootWrapper<Map<String, ScheduleV1>>

data class ScheduleV1Update(
    val name: String,
    val description: String,
    val command: ScheduleCommandV1,
    val localtime: String,
)

data class ScheduleCommandV1(
    val address: String,
    val method: Method,
    val body: ScheduleCommandBodyV1
)

data class ScheduleCommandBodyV1(
    val status: Int
)