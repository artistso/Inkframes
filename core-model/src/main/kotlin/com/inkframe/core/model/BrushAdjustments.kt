package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// BrushAdjustments
object BrushAdjustments {
    val SIZE_RANGE = 1f..512f
    val MIN_SIZE_RANGE = 0f..512f
    val OPACITY_RANGE = 0f..1f
    val FLOW_RANGE = 0f..1f
    val HARDNESS_RANGE = 0f..1f
    val SPACING_RANGE = 0.01f..1f
    val SMOOTHING_RANGE = 0f..0.95f

    fun withSize(b: Brush, v: Float): Brush {
        val nv = v.coerceIn(SIZE_RANGE.start, SIZE_RANGE.endInclusive)
        val nMin = minOf(b.minSizePx, nv)
        return b.copy(sizePx=nv, minSizePx=nMin)
    }
    fun withMinSize(b: Brush, v: Float): Brush {
        val nv = v.coerceIn(MIN_SIZE_RANGE.start, MIN_SIZE_RANGE.endInclusive).coerceAtMost(b.sizePx)
        return b.copy(minSizePx=nv)
    }
    fun withOpacity(b: Brush, v: Float) = b.copy(opacity=v.coerceIn(OPACITY_RANGE.start, OPACITY_RANGE.endInclusive))
    fun withFlow(b: Brush, v: Float) = b.copy(flow=v.coerceIn(FLOW_RANGE.start, FLOW_RANGE.endInclusive))
    fun withHardness(b: Brush, v: Float) = b.copy(hardness=v.coerceIn(HARDNESS_RANGE.start, HARDNESS_RANGE.endInclusive))
    fun withSpacing(b: Brush, v: Float) = b.copy(spacing=v.coerceIn(SPACING_RANGE.start, SPACING_RANGE.endInclusive))
    fun withSmoothing(b: Brush, v: Float) = b.copy(smoothing=v.coerceIn(SMOOTHING_RANGE.start, SMOOTHING_RANGE.endInclusive))
    fun withPressureToSize(b: Brush, v: Boolean) = b.copy(pressureToSize=v)
    fun withPressureToOpacity(b: Brush, v: Boolean) = b.copy(pressureToOpacity=v)
    fun withBuildUp(b: Brush, v: Boolean) = b.copy(buildUp=v)
    fun resetToDefault(b: Brush): Brush = DefaultBrushes.byId(b.id) ?: b
}

