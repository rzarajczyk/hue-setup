package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.configuration.GroupArchetype
import pl.zarajczyk.huesetup.hue.httpclient.*

abstract class GroupsModule(private val client: HueHttpClient, val type: RType) {

    fun list() = client.get<Groups>("/resource/$type").data
    private fun create(group: GroupUpdate) = client.post<ModificationResponse>("/resource/$type", group)
    private fun update(id: String, group: GroupUpdate) = client.put<ModificationResponse>("/resource/$type/$id", group)
    fun delete(id: String) = client.delete<ModificationResponse>("/resource/$type/$id")

    fun addChild(group: Group, ref: Reference) = update(
        id = group.id,
        group = GroupUpdate(
            children = group.children + ref
        )
    )


    fun removeChild(group: Group, ref: Reference) = update(
        id = group.id,
        group = GroupUpdate(
            children = group.children - ref
        )
    )

    fun create(name: String, type: RType, archetype: GroupArchetype) = create(
        GroupUpdate(
            type = type,
            children = emptyList(),
            metadata = GroupMetadataUpdate(
                name = name,
                archetype = archetype
            )
        )
    )

    fun changeArchetype(group: Group, archetype: GroupArchetype) = update(
        id = group.id,
        group = GroupUpdate(
            metadata = GroupMetadataUpdate(
                archetype = archetype
            )
        )
    )
}

data class Groups(
    val data: List<Group>
)

data class Group(
    override val id: String = "",
    override val id_v1: String = "",
    override val type: RType,
    val children: List<Reference>,
    val metadata: GroupMetadata
) : HueEntity, Resource

data class GroupMetadata(
    val name: String,
    val archetype: GroupArchetype
)

data class GroupUpdate(
    val type: RType? = null,
    val children: List<Reference>? = null,
    val metadata: GroupMetadataUpdate? = null
)

data class GroupMetadataUpdate(
    val name: String? = null,
    val archetype: GroupArchetype? = null
)