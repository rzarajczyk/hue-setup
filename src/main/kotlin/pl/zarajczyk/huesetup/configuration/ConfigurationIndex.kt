package pl.zarajczyk.huesetup.configuration

class ConfigurationIndex(definitions: RawDefinitions) {

    private val rooms = definitions.rooms.map { it.name }
    private val zones = definitions.zones.map { it.name }
    private val accessories = definitions.accessories.map { it.name }

    fun validateGroups(groups: List<String>) {
        groups
            .firstOrNull { it !in rooms && it !in zones }
            ?.let { throw RuntimeException("Unknown group ≪$it≫") }
    }

    fun validateGroup(group: String?) = if (group != null) validateGroups(listOf(group)) else Unit

    fun validateAccessory(name: String) = if (name !in accessories) throw RuntimeException("Unknown accessory ≪$name≫") else Unit

    fun isRoom(name: String) = name in rooms
    fun isZone(name: String) = name in zones

}