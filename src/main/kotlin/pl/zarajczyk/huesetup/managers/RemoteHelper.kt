package pl.zarajczyk.huesetup.managers

import pl.zarajczyk.huesetup.hue.apiclient.Device

sealed interface RemoteHelper {
    fun getButtonCode(button: String, pressType: String): Int
    fun getRuleName(sensorId: String, button: String, pressType: String): String

    companion object {
        fun get(device: Device) = when (device.product_data.model_id) {
            "RWL021" -> HueDimmerSwitchV1
            "RWL022" -> HueDimmerSwitchV2
            "ROM001" -> HueSmartButton
            else -> throw RuntimeException("Unsupported remote controller: ≪${device.product_data.model_id}≫")
        }

        fun listSupportedModelIds() = listOf("RWL021", "RWL022", "ROM001")
    }

}

private data object HueDimmerSwitchV1 : RemoteHelper {
    override fun getButtonCode(button: String, pressType: String): Int {
        val base = when (button) {
            "on" -> 1000
            "up" -> 2000
            "down" -> 3000
            "off" -> 4000
            else -> throw RuntimeException("Unsupported button: ≪$button≫ for ${this.javaClass.simpleName}")
        }
        return when (pressType) {
            "initial_press" -> base
            "repeat" -> base + 1
            "short_release" -> base + 2
            "long_release" -> base + 3
            "long_press" -> base + 4
            else -> throw RuntimeException("Unsupported press type: ≪$pressType≫ for ${this.javaClass.simpleName}")
        }
    }

    override fun getRuleName(sensorId: String, button: String, pressType: String): String {
        val buttonDesc = when (button) {
            "on" -> "on"
            "up" -> "up"
            "down" -> "down"
            "off" -> "off"
            else -> throw RuntimeException("Unsupported button: ≪$button≫ for ${this.javaClass.simpleName}")
        }
        val typeDesc = when (pressType) {
            "initial_press" -> "press"
            "repeat" -> "long"
            "short_release" -> "rele"
            "long_release" -> "long-rele"
            "long_press" -> "???"
            else -> throw RuntimeException("Unsupported press type: ≪$pressType≫ for ${this.javaClass.simpleName}")
        }
        return "Dimmer switch ${sensorId} ${buttonDesc}-${typeDesc}"
    }
}

private data object HueSmartButton : RemoteHelper {
    override fun getButtonCode(button: String, pressType: String): Int {
        val base = 1000
        return when (pressType) {
            "initial_press" -> base
            "repeat" -> base + 1
            "short_release" -> base + 2
            "long_release" -> base + 3
            "long_press" -> base + 10
            else -> throw RuntimeException("Unsupported press type: ≪$pressType≫ for ${this.javaClass.simpleName}")
        }
    }

    override fun getRuleName(sensorId: String, button: String, pressType: String) = "Hue smart button $sensorId"

}

private data object HueDimmerSwitchV2 : RemoteHelper {
    override fun getButtonCode(button: String, pressType: String): Int {
        val base = when (button) {
            "onoff" -> 1000
            "up" -> 2000
            "down" -> 3000
            "hue" -> 4000
            else -> throw RuntimeException("Unsupported button: ≪$button≫ for ${this.javaClass.simpleName}")
        }
        return when (pressType) {
            "initial_press" -> base
            "repeat" -> base + 1
            "short_release" -> base + 2
            "long_release" -> base + 3
            "long_press" -> base + 10
            else -> throw RuntimeException("Unsupported press type: ≪$pressType≫ for ${this.javaClass.simpleName}")
        }
    }

    override fun getRuleName(sensorId: String, button: String, pressType: String): String {
        val buttonDesc = when (button) {
            "onoff" -> "tgl"
            "up" -> "up"
            "down" -> "down"
            "hue" -> "hue"
            else -> throw RuntimeException("Unsupported button: ≪$button≫ for ${this.javaClass.simpleName}")
        }
        val typeDesc = when (pressType) {
            "initial_press" -> "press"
            "repeat" -> "long"
            "short_release" -> "rele"
            "long_release" -> "long-rele"
            "long_press" -> "???"
            else -> throw RuntimeException("Unsupported press type: ≪$pressType≫ for ${this.javaClass.simpleName}")
        }
        return "Dimmer switch ${sensorId} ${buttonDesc}-${typeDesc}"
    }

}