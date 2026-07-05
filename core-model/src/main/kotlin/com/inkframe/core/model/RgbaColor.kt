package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// RgbaColor
data class RgbaColor(val r: Float, val g: Float, val b: Float, val a: Float = 1f) {
    init {
        require(r in 0f..1f && g in 0f..1f && b in 0f..1f && a in 0f..1f) { "Color out of range" }
    }
    fun toArgb(): Int {
        val ai = (a.coerceIn(0f,1f)*255f+0.5f).toInt()
        val ri = (r.coerceIn(0f,1f)*255f+0.5f).toInt()
        val gi = (g.coerceIn(0f,1f)*255f+0.5f).toInt()
        val bi = (b.coerceIn(0f,1f)*255f+0.5f).toInt()
        return (ai shl 24) or (ri shl 16) or (gi shl 8) or bi
    }
    fun withAlpha(alpha: Float) = copy(a = alpha.coerceIn(0f,1f))
    companion object {
        val BLACK = RgbaColor(0f,0f,0f,1f)
        val WHITE = RgbaColor(1f,1f,1f,1f)
        val TRANSPARENT = RgbaColor(0f,0f,0f,0f)
        val RED = RgbaColor(1f,0f,0f,1f)
        val GREEN = RgbaColor(0f,1f,0f,1f)
        val BLUE = RgbaColor(0f,0f,1f,1f)
        val CYAN = RgbaColor(0f,1f,1f,1f)
        val YELLOW = RgbaColor(1f,1f,0f,1f)
        val MAGENTA = RgbaColor(1f,0f,1f,1f)
        fun fromArgb(argb: Int): RgbaColor {
            val a = ((argb ushr 24) and 0xFF)/255f
            val r = ((argb ushr 16) and 0xFF)/255f
            val g = ((argb ushr 8) and 0xFF)/255f
            val b = (argb and 0xFF)/255f
            return RgbaColor(r,g,b,a)
        }
    }
    override fun equals(other: Any?): Boolean {
        if (other !is RgbaColor) return false
        return abs(r-other.r)<1e-3f && abs(g-other.g)<1e-3f && abs(b-other.b)<1e-3f && abs(a-other.a)<1e-3f
    }
    override fun hashCode(): Int = toArgb()
}

