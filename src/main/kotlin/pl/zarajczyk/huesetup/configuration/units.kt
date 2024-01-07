package pl.zarajczyk.huesetup.configuration

import com.github.ajalt.colormath.model.RGB
import java.lang.Math.pow
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.roundToInt

data class ConfiguredGroupReference(private val name: String) {
    fun getGroupName() = name

    companion object {
        fun String.parseConfiguredGroupReference() = ConfiguredGroupReference(this)
    }
}

data class MacAddress(private val mac: String) {
    fun toMacAddress() = mac

    companion object {
        const val MAC_REGEXP =
            "[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}"

    }
}

data class ColorTemperature(private val mirek: Int) {
    fun tuHueMirek() = mirek

    companion object {
        const val COLOR_TEMPERATURE_REGEXP = "[0-9]{4}K"

        fun String.parseColorTemperature(): ColorTemperature {
            if (!this.endsWith("K")) {
                throw RuntimeException("Invalid brightness $this - doesn't end with K")
            }
            val result = this.removeSuffix("K").toInt()
            if (result < 2000 || result > 6500) {
                throw RuntimeException("Invalid brightness $this - out of range 0..100")
            }
            return ColorTemperature((1e6 / result.toDouble()).roundToInt())
        }
    }
}

data class Brightness(private val percent: BigDecimal) {
    fun toHueBrightnessPercentage() = percent

    companion object {
        const val BRIGHTNESS_REGEXP = "[0-9]{1,3}%"

        fun String.parseBrightness(): Brightness {
            if (!this.endsWith("%")) {
                throw RuntimeException("Invalid brightness $this - doesn't end with %")
            }
            val result = this.removeSuffix("%").toInt()
            if (result < 0 || result > 100) {
                throw RuntimeException("Invalid brightness $this - out of range 0..100")
            }
            return Brightness(BigDecimal.valueOf(result.toDouble()))
        }
    }
}

data class Color(private val x: Double, private val y: Double) {

    fun toHueX() = x
    fun toHueY() = y

    companion object {
        const val COLOR_REGEXP = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})"

        fun String.parseColor(): Color {
            val originalColor = RGB(this)
//            var r = originalColor.r
//            var g = originalColor.g
//            var b = originalColor.b
//            val red = if ((r > 0.04045f)) ((r + 0.055f) / (1.0f + 0.055f)).toDouble().pow(2.4).toFloat() else (r / 12.92f)
//            val green = if ((g > 0.04045f)) ((g + 0.055f) / (1.0f + 0.055f)).toDouble().pow(2.4).toFloat() else (g / 12.92f)
//            val blue = if ((b > 0.04045f)) ((b + 0.055f) / (1.0f + 0.055f)).toDouble().pow(2.4).toFloat() else (b / 12.92f)
//            val colorAfterGammaCorrection = RGB(red, green, blue)
            val x = originalColor.toXYZ().toCIExyY().x.toDouble()
            val y = originalColor.toXYZ().toCIExyY().y.toDouble()
            return Color(x, y)
        }
    }
}

data class LocalTime(private val localtime: String) {
    fun toHueLocalTime() = localtime

    companion object {
        const val TIME_REGEXP = "W127/T[0-9]{2}:[0-9]{2}:[0-9]{2}"

    }
}

data class LocalTimePeriod(private val localtime: String) {
    fun toHueLocalTimePeriod() = localtime

    companion object {
        const val TIME_PERIOD_REGEXP = "T[0-9]{2}:[0-9]{2}:[0-9]{2}/T[0-9]{2}:[0-9]{2}:[0-9]{2}"
    }
}

data class TransitionTime(private val hueTime: Int) {
    fun toDeciSeconds() = hueTime

    companion object {
        const val TRANSITION_TIME_REGEXP = "[0-9]{1,4}(min|s|ds)"
        fun String.parseTransitionTime(): TransitionTime {
            val hueTime = when {
                this.endsWith("min") -> this.removeSuffix("min").trim().toInt() * 10 * 60
                this.endsWith("ds") -> this.removeSuffix("ds").trim().toInt()
                this.endsWith("s") -> this.removeSuffix("s").trim().toInt() * 10
                else -> throw RuntimeException("Invalid transition time: $this")
            }
            return TransitionTime(hueTime)
        }
    }
}

data class Timeout(private val seconds: Int) {
    fun toDuration(): String {
        val mins = (seconds / 60).toString().padStart(2, '0')
        val secs = (seconds % 60).toString().padStart(2, '0')
        return "PT00:$mins:$secs"
    }

    companion object {
        const val TIMEOUT_REGEXP = "[0-9]{1,4}(min|s)"
        fun String.parseTimeout(): Timeout {
            val hueTime = when {
                this.endsWith("min") -> this.removeSuffix("min").trim().toInt() * 60
                this.endsWith("s") -> this.removeSuffix("s").trim().toInt()
                else -> throw RuntimeException("Invalid timeout: $this")
            }
            return Timeout(hueTime)
        }
    }
}