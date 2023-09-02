package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.configuration.*
import pl.zarajczyk.huesetup.hue.apiclient.V1RuleAction
import pl.zarajczyk.huesetup.hue.apiclient.V1RuleCondition
import pl.zarajczyk.huesetup.hue.apiclient.V1RuleUpdate
import pl.zarajczyk.huesetup.hue.apiclient.plainId
import pl.zarajczyk.huesetup.hue.systemclient.HueSystemClient

class AutomationsManager(
    private val automations: List<AutomationDefinition>,
    private val hue: HueSystemClient
) : Manager {
    override fun run() {
        print("Deleting existing rules, memory sensors and schedules...")
        System.out.flush()
        hue.apiClient.v1_rules.deleteAll()
        hue.apiClient.v1_sensors.deleteAllMemorySensors()
        hue.apiClient.v1_schedules.deleteAll()
        println(" - ✔ DELETED")


        hue.refresh()

        automations.forEach {
            when (it) {
                is TimeAutomationDefinition -> createTimeBasedAutomation(it)
                is ButtonAutomationDefinition -> createButtonAutomation(it)
                is MotionAutomationDefinition -> createMotionAutomation(it)
            }
        }
    }

    private fun createTimeBasedAutomation(def: TimeAutomationDefinition) {
        print("Will create time-based automation: ${def.name} (${def.time.toHueLocalTime()})")
        System.out.flush()
        val createSensorResponse = hue.apiClient.v1_sensors.createMemorySensor(def.name)
        hue.apiClient.v1_schedules.createScheduleForUpdatingSensor(
            name = def.name,
            sensorId = createSensorResponse.sensorId,
            localtime = def.time.toHueLocalTime()
        )

        val sensorUpdatedCondition = V1RuleCondition(
            address = "/sensors/${createSensorResponse.sensorId}/state/lastupdated",
            operator = "dx",
            value = null
        )

        def.actions
            .map { createRule(def.name, it, sensorUpdatedCondition) }
            .forEach { hue.apiClient.v1_rules.create(it) }

        println(" - ✔ CREATED")
    }

    fun createButtonAutomation(def: ButtonAutomationDefinition) {
        val device = hue.findDeviceByName(def.remote)
        val helper = RemoteHelper.get(device)
        val sensorId = device.id_v1 ?: throw RuntimeException("Remote controller ≪${def.remote}≫ doesn't have v1_id")
        val name = helper.getRuleName(sensorId.plainId(), def.button, def.pressType)
        print("Will create button automation: $name (${def.button})")
        System.out.flush()

        val buttonEventRuleCondition = V1RuleCondition(
            address = "$sensorId/state/buttonevent",
            operator = "eq",
            value = "${helper.getButtonCode(def.button, def.pressType)}"
        )

        val sensorUpdatedCondition = V1RuleCondition(
            address = "${sensorId}/state/lastupdated",
            operator = "dx",
            value = null
        )

        def.actions
            .map { createRule(name, it, buttonEventRuleCondition, sensorUpdatedCondition) }
            .forEach {hue.apiClient.v1_rules.create(it) }

        println(" - ✔ CREATED")
    }

    fun createMotionAutomation(def: MotionAutomationDefinition) {
        val device = hue.findDeviceByName(def.sensor)
        val sensorId = device.id_v1 ?: throw RuntimeException("Remote controller ≪${def.sensor}≫ doesn't have v1_id")
        val name = "Motion ${sensorId.plainId()}"
        print("Will create motion automation: $name")
        System.out.flush()

        val motionCondition = V1RuleCondition(
            address = "$sensorId/state/presence",
            operator = "eq",
            value = "${def.motion}"
        )

        val sensorUpdatedCondition = V1RuleCondition(
            address = "${sensorId}/state/presence",
            operator = "dx",
            value = null
        )

        def.actions
            .map { createRule(name, it, motionCondition, sensorUpdatedCondition) }
            .forEach {hue.apiClient.v1_rules.create(it) }

        println(" - ✔ CREATED")
    }

    fun createWaitAutomation(name: String, timeout: Timeout, actions: List<AutomationAction>): String {
        val sensorId = hue.apiClient.v1_sensors.createMemorySensor(name).sensorId
        print("\n    - Will create wait automation: $name")
        System.out.flush()

        val sensorStateCondition = V1RuleCondition(
            address = "/sensors/$sensorId/state/status",
            operator = "eq",
            value = "1"
        )

        val sensorUpdatedCondition = V1RuleCondition(
            address = "/sensors/$sensorId/state/lastupdated",
            operator = "ddx",
            value = timeout.toDuration()
        )

        actions
            .map { createRule(name, it, sensorStateCondition, sensorUpdatedCondition) }
            .forEach {hue.apiClient.v1_rules.create(it) }

        println(" - ✔ CREATED")

        return sensorId
    }

    private fun createRule(
        name: String,
        action: AutomationAction,
        vararg additionalConditions: V1RuleCondition
    ): V1RuleUpdate {
        return V1RuleUpdate(
            name = name,
            actions = createActions(action, name),
            conditions = createConditions(action) + additionalConditions.toList()
        )
    }

    private fun createActions(action: AutomationAction, name: String): List<V1RuleAction> = listOf(
        when (action) {
            is TurnOnAutomationAction -> V1RuleAction(
                address = "${hue.findGroup(action.group).id_v1}/action",
                method = "PUT",
                body = mapOf(
                    "on" to true,
                    "transitiontime" to action.transitionTime.toDeciSeconds()
                )
            )

            is TurnOffAutomationAction -> V1RuleAction(
                address = "${hue.findGroup(action.group).id_v1}/action",
                method = "PUT",
                body = mapOf(
                    "on" to false,
                    "transitiontime" to action.transitionTime.toDeciSeconds()
                )
            )

            is ColorTemperatureTransitionAutomationAction -> V1RuleAction(
                address = "${hue.findGroup(action.group).id_v1}/action",
                method = "PUT",
                body = mapOf(
                    "ct" to action.colorTemperature.tuHueMirek(),
                    "transitiontime" to action.transitionTime.toDeciSeconds()
                ),
            )

            is SceneAutomationAction -> V1RuleAction(
                address = "${hue.findGroup(action.group).id_v1}/action",
                method = "PUT",
                body = mapOf(
                    "scene" to hue.findScene(action.group, action.scene).id_v1.plainId(),
                    "transitiontime" to action.transitionTime.toDeciSeconds()
                ),
            )

            is DisableSensorAction -> V1RuleAction(
                address = "${hue.findDeviceByName(action.sensor).id_v1}/config",
                method = "PUT",
                body = mapOf("on" to false)
            )

            is EnableSensorAction -> V1RuleAction(
                address = "${hue.findDeviceByName(action.sensor).id_v1}/config",
                method = "PUT",
                body = mapOf("on" to true)
            )

            is WaitAutomationAction -> {
                val sensorId = createWaitAutomation(name, action.duration, action.actions)
                V1RuleAction(
                    address = "/sensors/$sensorId/state",
                    method = "PUT",
                    body = mapOf("status" to 1)
                )
            }
        }
    )

    private fun createConditions(action: AutomationAction): List<V1RuleCondition> {
        return action.conditions.map { condition ->
            when (condition) {
                is AnyOnCondition -> V1RuleCondition(
                    address = "${hue.findGroup(condition.group).id_v1}/state/any_on",
                    operator = "eq",
                    value = "true"
                )

                is AllOffCondition -> V1RuleCondition(
                    address = "${hue.findGroup(condition.group).id_v1}/state/any_on",
                    operator = "eq",
                    value = "false"
                )

                is TimeOfDayActionCondition -> V1RuleCondition(
                    address = "/config/localtime",
                    operator = "in",
                    value = condition.time.toHueLocalTimePeriod()
                )
            }
        }
    }
}