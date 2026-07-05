package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// Onion skin
data class OnionSkinSettings(
    val framesBefore: Int = 2,
    val framesAfter: Int = 2,
    val nearOpacity: Float = 0.5f,
    val farOpacity: Float = 0.15f,
    val tintStrength: Float = 0.5f,
    val enabled: Boolean = true,
    val beforeTint: RgbaColor = RgbaColor(1f,0f,0f),
    val afterTint: RgbaColor = RgbaColor(0f,0f,1f)
) {
    init {
        require(framesBefore in 0..MAX_RANGE) { "framesBefore out of range" }
        require(framesAfter in 0..MAX_RANGE) { "framesAfter out of range" }
    }
    companion object {
        const val MAX_RANGE = 8
    }
}
data class OnionGhost(
    val frame: Int,
    val surfaceId: Long,
    val opacity: Float,
    val tint: RgbaColor,
    val tintStrength: Float = 0.5f,
    val offset: Int = 0
)
object OnionSkinPlanner {
    fun plan(currentFrame: Int, settings: OnionSkinSettings, surfaceAt: (Int)->Long?): List<OnionGhost> {
        if (!settings.enabled) return emptyList()
        val out = mutableListOf<OnionGhost>()
        // before
        for (i in 1..settings.framesBefore) {
            val offset = -i
            val f = currentFrame + offset
            val sid = surfaceAt(f) ?: continue
            val t = if (settings.framesBefore <=1) 0f else (i-1).toFloat()/(settings.framesBefore-1)
            val opacity = settings.nearOpacity + (settings.farOpacity - settings.nearOpacity) * t
            out.add(OnionGhost(f, sid, opacity, settings.beforeTint, settings.tintStrength, offset))
        }
        // after
        for (i in 1..settings.framesAfter) {
            val offset = i
            val f = currentFrame + offset
            val sid = surfaceAt(f) ?: continue
            val t = if (settings.framesAfter <=1) 0f else (i-1).toFloat()/(settings.framesAfter-1)
            val opacity = settings.nearOpacity + (settings.farOpacity - settings.nearOpacity) * t
            out.add(OnionGhost(f, sid, opacity, settings.afterTint, settings.tintStrength, offset))
        }
        // farthest first
        return out.sortedByDescending { kotlin.math.abs(it.offset) }
    }
}

