package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// TimelineDrag
object TimelineDrag {
    fun frameAt(x: Float, cellW: Float, gap: Float, startOffset: Float = 0f): Int {
        val pitch = cellW + gap
        if (pitch <= 0f) return 0
        return ((x - startOffset)/pitch).toInt().coerceAtLeast(0)
    }
    fun resolveDrag(startX: Float, endX: Float, cellW: Float, gap: Float, hasCel: (Int)->Boolean): Pair<Int,Int>? {
        val src = frameAt(startX, cellW, gap)
        val dst = frameAt(endX, cellW, gap)
        if (!hasCel(src)) return null
        return src to dst
    }
}

