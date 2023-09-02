package pl.zarajczyk.huesetup.hue.apiclient

data class Reference(
    val rid: String,
    val rtype: RType
)

@Suppress("unused")
enum class RType {
    device, bridge_home, room, zone, light, button, relative_rotary, temperature, light_level, motion, camera_motion, entertainment, contact, tamper, grouped_light, device_power, zigbee_bridge_connectivity, zigbee_connectivity, zgp_connectivity, bridge, zigbee_device_discovery, homekit, matter, matter_fabric, scene, entertainment_configuration, public_image, auth_v1, behavior_script, behavior_instance, geofence, geofence_client, geolocation, smart_scene,
    taurus_7455, device_software_update
}

enum class Archetype {
    bridge_v2, unknown_archetype, classic_bulb, sultan_bulb, flood_bulb, spot_bulb, candle_bulb, luster_bulb, pendant_round, pendant_long, ceiling_round, ceiling_square, floor_shade, floor_lantern, table_shade, recessed_ceiling, recessed_floor, single_spot, double_spot, table_wash, wall_lantern, wall_shade, flexible_lamp, ground_spot, wall_spot, plug, hue_go, hue_lightstrip, hue_iris, hue_bloom, bollard, wall_washer, hue_play, vintage_bulb, vintage_candle_bulb, ellipse_bulb, triangle_bulb, small_globe_bulb, large_globe_bulb, edison_bulb, christmas_tree, string_light, hue_centris, hue_lightstrip_tv, hue_lightstrip_pc, hue_tube, hue_signe, pendant_spot, ceiling_horizontal, ceiling_tube
}

data class ModificationResponse(
    val errors: List<String>,
    val data: List<Reference>
)

fun String.plainId() = this.split("/").last()