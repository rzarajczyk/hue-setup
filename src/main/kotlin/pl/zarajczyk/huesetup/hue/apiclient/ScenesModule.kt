package pl.zarajczyk.huesetup.hue.apiclient

import pl.zarajczyk.huesetup.hue.httpclient.*
import java.math.BigDecimal

class ScenesModule(private val client: HueHttpClient) {

    fun list() = client.get<Scenes>("/resource/scene", ApiVersion.V2).data

    fun delete(id: String) = client.delete<ModificationResponse>("/resource/scene/$id", ApiVersion.V2)

    private fun create(scene: SceneUpdate) = client.post<ModificationResponse>("/resource/scene", scene, ApiVersion.V2)

    private fun update(id: String, scene: SceneUpdate) =
        client.put<ModificationResponse>("/resource/scene/$id", scene, ApiVersion.V2)

    fun create(name: String, group: Reference, actions: List<SceneAction>) = create(
        SceneUpdate(
            metadata = SceneMetadataUpdate(
                name = name
            ),
            group = group,
            actions = actions
        )
    )

    fun changeActions(scene: Scene, actions: List<SceneAction>) = update(
        id = scene.id,
        scene = SceneUpdate(
            actions = actions
        )
    )

}

data class Scenes(
    val data: List<Scene>
)

data class Scene(
    override val id: String,
    override val id_v1: String,
    override val type: RType,
    val metadata: SceneMetadata,
    val group: Reference,
    val speed: BigDecimal,
    val actions: List<SceneAction>
) : HueEntity, Resource

data class SceneUpdate(
    val actions: List<SceneAction>? = null,
    val metadata: SceneMetadataUpdate? = null,
    val group: Reference? = null,
)

data class SceneMetadata(
    val name: String,
    val appdata: String?
)

data class SceneMetadataUpdate(
    val name: String? = null,
    val appdata: String? = null
)

data class SceneAction(
    val target: Reference,
    val action: Action
)

data class Action(
    val on: OnAction?,
    val dimming: DimmingAction?,
    val color_temperature: ColorTemperatureAction?
)

data class OnAction(
    val on: Boolean
)

data class DimmingAction(
    val brightness: BigDecimal
)

data class ColorTemperatureAction(
    val mirek: Int
)