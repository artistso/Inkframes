package com.inkframe.core.model

import com.inkframe.core.common.*
import kotlin.math.*

object ExportPlanner {
    data class ExportFrame(val frameIndex: Int, val durationMs: Int) {
        val gifDelayCs: Int get() = msToCentisecondsRounded(durationMs)
        // compatibility shims for older engine code expecting .frame
        val frame: Int get() = frameIndex
    }
    data class ExportPlan(
        val frames: List<ExportFrame>,
        val fps: Int,
        val widthPx: Int,
        val heightPx: Int,
        val loop: Boolean
    ) {
        val frameCount get() = frames.size
        val totalDurationMs get() = frames.sumOf { it.durationMs }
        // compatibility for older code that expects width/height without Px
        val width: Int get() = widthPx
        val height: Int get() = heightPx
    }

    enum class Range { ALL, PLAYBACK }
    fun plan(
        scene: Scene,
        canvas: CanvasSpec,
        range: Range = Range.PLAYBACK,
        fpsOverride: Int? = null,
        frameStep: Int = 1
    ): ExportPlan {
        require(frameStep > 0) { "frameStep must >0" }
        val fps = fpsOverride ?: canvas.fps
        val r = when(range) {
            Range.ALL -> 0 until scene.frameCount
            Range.PLAYBACK -> scene.playbackRange
        }
        val frameMs = 1000.0 / fps
        val durationMs = (frameMs * frameStep).toInt()
        val frames = mutableListOf<ExportFrame>()
        var f = r.first
        while (f <= r.last) {
            frames.add(ExportFrame(f, durationMs))
            f += frameStep
        }
        return ExportPlan(frames, fps, canvas.widthPx, canvas.heightPx, scene.loop)
    }
    // compatibility overload
    fun plan(scene: Scene, canvas: CanvasSpec, fpsOverride: Int?, step: Int): ExportPlan =
        plan(scene, canvas, Range.PLAYBACK, fpsOverride, step)

    fun msToCentisecondsRounded(ms: Int): Int {
        val cs = (ms + 5) / 10
        return if (cs < 2) 2 else cs
    }
    fun frameFileName(prefix: String, index: Int, total: Int): String {
        val width = total.toString().length.coerceAtLeast(4)
        return "%s_%0${width}d.png".format(prefix, index)
    }
}

