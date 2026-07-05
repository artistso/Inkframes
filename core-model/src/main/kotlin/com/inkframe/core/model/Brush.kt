package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// Brush
enum class BrushKind { INK, ROUND, AIRBRUSH, ERASER }
data class Brush(
    val id: String,
    val name: String,
    val kind: BrushKind = BrushKind.ROUND,
    val sizePx: Float = 24f,
    val minSizePx: Float = 2f,
    val opacity: Float = 1f,
    val flow: Float = 1f,
    val hardness: Float = 0.8f,
    val spacing: Float = 0.15f,
    val smoothing: Float = 0.3f,
    val stabilization: Float = 0f,
    val pressureToSize: Boolean = true,
    val pressureToOpacity: Boolean = true,
    val buildUp: Boolean = false,
    val glowTrail: Boolean = false
) {
    fun diameterForPressure(p: Float): Float {
        val pp = p.coerceIn(0f,1f)
        return if (pressureToSize) minSizePx + (sizePx-minSizePx)*pp else sizePx
    }
    fun flowForPressure(p: Float): Float {
        val pp = p.coerceIn(0f,1f)
        return (if (pressureToOpacity) flow*pp else flow).coerceIn(0f,1f)
    }
}

object DefaultBrushes {
    val ink = Brush(id="ink", name="Ink", kind=BrushKind.INK, sizePx=18f, minSizePx=1f, hardness=0.95f, spacing=0.12f, smoothing=0.25f, buildUp=false)
    val round = Brush(id="round", name="Round", kind=BrushKind.ROUND, sizePx=32f, minSizePx=2f, hardness=0.7f, spacing=0.15f, smoothing=0.3f, buildUp=false)
    val airbrush = Brush(id="airbrush", name="Airbrush", kind=BrushKind.AIRBRUSH, sizePx=80f, minSizePx=20f, hardness=0.1f, spacing=0.08f, smoothing=0.4f, flow=0.5f, buildUp=true, pressureToOpacity=true)
    val all = listOf(ink, round, airbrush)
    fun byId(id: String) = all.find { it.id==id }
}

