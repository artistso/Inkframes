package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// Canvas / Cel / Layer / Scene / Project
data class CanvasSpec(
    val widthPx: Int,
    val heightPx: Int,
    val fps: Int,
    val pixelAspect: Float = 1f,
    val background: RgbaColor = RgbaColor.WHITE
)

data class CelTransform(
    val tx: Float = 0f,
    val ty: Float = 0f,
    val rotationDeg: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)

data class Cel(
    val id: String = "cel-${System.nanoTime()}",
    val surfaceId: Long,
    val transform: CelTransform = CelTransform()
)

enum class BlendMode(val displayName: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    ADD("Add"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    ERASE("Erase");
    companion object {
        fun fromOrdinalSafe(o: Int): BlendMode = values().getOrElse(o) { NORMAL }
    }
}

data class Layer(
    val id: String = "layer-${(0..999999).random()}",
    val name: String,
    val opacity: Float = 1f,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val cels: Map<Int, Cel> = emptyMap()
) {
    init { require(opacity in 0f..1f) { "opacity out of range" } }
    fun celAt(frame: Int): Cel? {
        if (cels.isEmpty()) return null
        val keys = cels.keys.filter { it <= frame }
        if (keys.isEmpty()) return null
        return cels[keys.maxOrNull()]
    }
}

data class Scene(
    val id: String = "scene-${(0..999999).random()}",
    val name: String,
    val frameCount: Int = 24,
    val layers: List<Layer> = listOf(Layer(name="Layer 1")),
    val playbackRange: IntRange = 0 until frameCount,
    val loop: Boolean = true
) {
    init { require(frameCount >= 1) { "frameCount must >=1" } }
    fun layerById(id: String) = layers.find { it.id == id }
}

data class Project(
    val id: String,
    val name: String,
    val canvas: CanvasSpec,
    val scenes: List<Scene> = emptyList(),
    val activeSceneId: String? = scenes.firstOrNull()?.id,
    val colorPalette: List<RgbaColor> = emptyList(),
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val modifiedAtEpochMs: Long = System.currentTimeMillis()
) {
    val activeScene: Scene? get() = scenes.find { it.id == activeSceneId } ?: scenes.firstOrNull()
}

