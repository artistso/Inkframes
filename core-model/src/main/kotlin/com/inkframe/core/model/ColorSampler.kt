package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// ColorSampler
object ColorSampler {
    fun sampleAt(pixels: IntArray, w: Int, h: Int, x: Int, y: Int): RgbaColor? {
        if (x<0||y<0||x>=w||y>=h) return null
        require(pixels.size >= w*h) { "Undersized array" }
        val argb = pixels[y*w + x]
        return RgbaColor.fromArgb(argb)
    }
    fun sampleAverage(pixels: IntArray, w: Int, h: Int, cx: Int, cy: Int, radius: Int): RgbaColor? {
        var rSum = 0L; var gSum=0L; var bSum=0L; var aSum=0L; var count=0
        for (dy in -radius..radius) for (dx in -radius..radius) {
            val x = cx+dx; val y = cy+dy
            if (x<0||y<0||x>=w||y>=h) continue
            val argb = pixels[y*w + x]
            val a = (argb ushr 24) and 0xFF
            if (a==0) continue
            rSum += (argb ushr 16) and 0xFF
            gSum += (argb ushr 8) and 0xFF
            bSum += argb and 0xFF
            aSum += a
            count++
        }
        if (count==0) return null
        return RgbaColor(
            rSum.toFloat()/count/255f,
            gSum.toFloat()/count/255f,
            bSum.toFloat()/count/255f,
            aSum.toFloat()/count/255f
        )
    }
}

