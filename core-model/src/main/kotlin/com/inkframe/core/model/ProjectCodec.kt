package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// ProjectCodec
object ProjectCodec {
    const val FORMAT_VERSION = 1
    fun toJsonString(p: Project): String = toJson(p).toJsonString(pretty=true)
    fun fromJsonString(s: String): Project = fromJson(parseJson(s))
    fun toJson(p: Project): JsonValue = JsonValue.obj(
        "version" to JsonValue.of(FORMAT_VERSION),
        "id" to JsonValue.of(p.id),
        "name" to JsonValue.of(p.name),
        "canvas" to JsonValue.obj(
            "width" to JsonValue.of(p.canvas.widthPx),
            "height" to JsonValue.of(p.canvas.heightPx),
            "fps" to JsonValue.of(p.canvas.fps),
            "pixelAspect" to JsonValue.of(p.canvas.pixelAspect)
        ),
        "scenes" to JsonValue.arr(p.scenes.map { sceneToJson(it) })
    )
    private fun sceneToJson(s: Scene) = JsonValue.obj(
        "id" to JsonValue.of(s.id),
        "name" to JsonValue.of(s.name),
        "frameCount" to JsonValue.of(s.frameCount),
        "playbackRange" to JsonValue.arr(listOf(JsonValue.of(s.playbackRange.first), JsonValue.of(s.playbackRange.last))),
        "loop" to JsonValue.of(s.loop),
        "layers" to JsonValue.arr(s.layers.map { layerToJson(it) })
    )
    private fun layerToJson(l: Layer) = JsonValue.obj(
        "id" to JsonValue.of(l.id),
        "name" to JsonValue.of(l.name),
        "opacity" to JsonValue.of(l.opacity),
        "visible" to JsonValue.of(l.visible),
        "locked" to JsonValue.of(l.locked),
        "blendMode" to JsonValue.of(l.blendMode.name),
        "cels" to JsonValue.arr(l.cels.map { (frame,cel) ->
            JsonValue.obj(
                "frame" to JsonValue.of(frame),
                "id" to JsonValue.of(cel.id),
                "surfaceId" to JsonValue.of(cel.surfaceId.toInt()),
                "transform" to JsonValue.obj(
                    "tx" to JsonValue.of(cel.transform.tx),
                    "ty" to JsonValue.of(cel.transform.ty),
                    "rotationDeg" to JsonValue.of(cel.transform.rotationDeg)
                )
            )
        })
    )
    fun fromJson(v: JsonValue): Project {
        val version = v.optional("version")?.asInt() ?: 1
        require(version <= FORMAT_VERSION) { "Newer format version $version" }
        val id = v["id"].asString()
        val name = v["name"].asString()
        val canvasObj = v["canvas"].asObj()
        val canvas = CanvasSpec(
            widthPx = canvasObj.entries["width"]!!.asInt(),
            heightPx = canvasObj.entries["height"]!!.asInt(),
            fps = canvasObj.entries["fps"]!!.asInt(),
            pixelAspect = canvasObj.entries["pixelAspect"]?.asFloat() ?: 1f
        )
        val scenesArr = v.optional("scenes")?.asArr()?.items ?: emptyList()
        val scenes = scenesArr.map { parseScene(it) }
        return Project(id=id, name=name, canvas=canvas, scenes=scenes, activeSceneId=scenes.firstOrNull()?.id)
    }
    private fun parseScene(v: JsonValue): Scene {
        val o = v.asObj()
        val id = o.entries["id"]!!.asString()
        val name = o.entries["name"]!!.asString()
        val frameCount = o.entries["frameCount"]!!.asInt()
        val playback = o.entries["playbackRange"]?.asArr()
        val range = if (playback!=null) playback.items[0].asInt() .. playback.items[1].asInt() else 0 until frameCount
        val loop = o.entries["loop"]?.asBool() ?: true
        val layers = o.entries["layers"]!!.asArr().items.map { parseLayer(it) }
        return Scene(id=id, name=name, frameCount=frameCount, layers=layers, playbackRange=range, loop=loop)
    }
    private fun parseLayer(v: JsonValue): Layer {
        val o = v.asObj()
        val id = o.entries["id"]?.asString() ?: "layer-${(0..99999).random()}"
        val name = o.entries["name"]!!.asString()
        val opacity = o.entries["opacity"]?.asFloat() ?: 1f
        val visible = o.entries["visible"]?.asBool() ?: true
        val locked = o.entries["locked"]?.asBool() ?: false
        val blendMode = try { BlendMode.valueOf(o.entries["blendMode"]?.asString() ?: "NORMAL") } catch (e: Exception) { BlendMode.NORMAL }
        val celsArr = o.entries["cels"]!!.asArr().items
        val cels = celsArr.associate { celV ->
            val co = celV.asObj()
            val frame = co.entries["frame"]!!.asInt()
            val cid = co.entries["id"]?.asString() ?: "cel-$frame"
            val sid = co.entries["surfaceId"]!!.asLong()
            val tObj = co.entries["transform"]?.asObj()
            val transform = if (tObj!=null) CelTransform(
                tx = tObj.entries["tx"]?.asFloat() ?: 0f,
                ty = tObj.entries["ty"]?.asFloat() ?: 0f,
                rotationDeg = tObj.entries["rotationDeg"]?.asFloat() ?: 0f
            ) else CelTransform()
            frame to Cel(id=cid, surfaceId=sid, transform=transform)
        }
        return Layer(id=id, name=name, opacity=opacity, visible=visible, locked=locked, blendMode=blendMode, cels=cels)
    }
}

