package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// PlaybackOps
object PlaybackOps {
    fun clampFps(fps: Int) = fps.coerceIn(1,120)
    fun frameDurationMs(fps: Int) = 1000f / clampFps(fps)
    fun setInPoint(range: IntRange, newIn: Int, lastFrame: Int): IntRange {
        val ni = newIn.coerceIn(0, lastFrame)
        val out = range.last.coerceAtLeast(ni)
        return ni..out
    }
    fun setOutPoint(range: IntRange, newOut: Int, lastFrame: Int): IntRange {
        val no = newOut.coerceIn(0, lastFrame)
        val inp = range.first.coerceAtMost(no)
        return inp..no
    }
    fun clampRange(range: IntRange, lastFrame: Int): IntRange {
        val s = range.first.coerceIn(0,lastFrame)
        val e = range.last.coerceIn(s,lastFrame)
        return s..e
    }
    fun fullRange(lastFrame: Int) = 0..lastFrame
    fun length(range: IntRange) = range.last - range.first + 1
    fun nextFrame(current: Int, range: IntRange, loop: Boolean): Pair<Int,Boolean> {
        if (current < range.first || current > range.last) return range.first to true
        val next = current + 1
        return if (next > range.last) {
            if (loop) range.first to true else range.last to false
        } else next to true
    }
}

