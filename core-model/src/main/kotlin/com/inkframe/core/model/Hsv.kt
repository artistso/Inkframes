package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// Hsv
data class Hsv(val h: Float, val s: Float, val v: Float, val a: Float = 1f) {
    fun toRgba(): RgbaColor {
        val hh = wrapHue(h)
        val c = v.coerceIn(0f,1f) * s.coerceIn(0f,1f)
        val x = c * (1 - abs((hh/60f)%2 -1))
        val m = v.coerceIn(0f,1f) - c
        val (rp,gp,bp) = when {
            hh < 60 -> Triple(c,x,0f)
            hh < 120 -> Triple(x,c,0f)
            hh < 180 -> Triple(0f,c,x)
            hh < 240 -> Triple(0f,x,c)
            hh < 300 -> Triple(x,0f,c)
            else -> Triple(c,0f,x)
        }
        return RgbaColor((rp+m).coerceIn(0f,1f), (gp+m).coerceIn(0f,1f), (bp+m).coerceIn(0f,1f), a.coerceIn(0f,1f))
    }
    fun withHue(nh: Float) = copy(h = wrapHue(nh))
    fun withSaturation(ns: Float) = copy(s = ns.coerceIn(0f,1f))
    fun withValue(nv: Float) = copy(v = nv.coerceIn(0f,1f))
    fun withAlpha(na: Float) = copy(a = na.coerceIn(0f,1f))
    fun normalized() = Hsv(wrapHue(h), s.coerceIn(0f,1f), v.coerceIn(0f,1f), a.coerceIn(0f,1f))
    companion object {
        fun wrapHue(v: Float): Float { var x = v % 360f; if (x < 0) x += 360f; if (x >= 360f) x -= 360f; return x }
        fun fromRgba(c: RgbaColor): Hsv {
            val r=c.r; val g=c.g; val b=c.b
            val max = maxOf(r,g,b); val min = minOf(r,g,b)
            val delta = max-min
            val v = max
            val s = if (max==0f) 0f else delta/max
            val h = when {
                delta==0f -> 0f
                max==r -> 60f * (((g-b)/delta) % 6)
                max==g -> 60f * (((b-r)/delta)+2)
                else -> 60f * (((r-g)/delta)+4)
            }
            return Hsv(wrapHue(h), s, v, c.a)
        }
    }
}

