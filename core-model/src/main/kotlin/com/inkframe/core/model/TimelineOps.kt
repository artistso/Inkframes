package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// TimelineOps
object TimelineOps {
    fun explicitCel(layer: Layer, frame: Int): Cel? = layer.cels[frame]
    fun clearCel(layer: Layer, frame: Int): Layer {
        val newCels = layer.cels.toMutableMap(); newCels.remove(frame); return layer.copy(cels=newCels)
    }
    fun setCel(layer: Layer, frame: Int, cel: Cel): Layer {
        val newCels = layer.cels.toMutableMap(); newCels[frame]=cel; return layer.copy(cels=newCels)
    }
    fun moveCel(layer: Layer, from: Int, to: Int): Layer {
        val cel = layer.cels[from] ?: return layer
        val m = layer.cels.toMutableMap(); m.remove(from); m[to]=cel; return layer.copy(cels=m)
    }
    fun duplicateCel(layer: Layer, from: Int, to: Int, newSurfaceId: Long): Layer {
        val src = layer.cels[from] ?: return layer
        val copy = src.copy(id="cel-${newSurfaceId}", surfaceId=newSurfaceId)
        return setCel(layer, to, copy)
    }
    fun pasteCel(layer: Layer, frame: Int, srcCel: Cel, newSurfaceId: Long): Layer {
        val copy = srcCel.copy(id="cel-${newSurfaceId}", surfaceId=newSurfaceId)
        return setCel(layer, frame, copy)
    }
    fun shiftCels(layer: Layer, startFrame: Int, delta: Int): Layer {
        val newMap = mutableMapOf<Int,Cel>()
        layer.cels.forEach { (f,c) -> val nf = if (f>=startFrame) f+delta else f; if (nf>=0) newMap[nf]=c }
        return layer.copy(cels=newMap)
    }
    fun insertFrames(scene: Scene, at: Int, count: Int): Scene {
        val newLayers = scene.layers.map { shiftCels(it, at, count) }
        val newFrameCount = scene.frameCount + count
        val pr = scene.playbackRange
        val newRange = (if (pr.first >= at) pr.first+count else pr.first)..(pr.last+count)
        return scene.copy(layers=newLayers, frameCount=newFrameCount, playbackRange=newRange)
    }
    fun removeFrames(scene: Scene, at: Int, count: Int): Scene {
        val newLayers = scene.layers.map { layer ->
            val m = mutableMapOf<Int,Cel>()
            layer.cels.forEach { (f,c) ->
                when {
                    f < at -> m[f]=c
                    f >= at+count -> m[f-count]=c
                }
            }
            layer.copy(cels=m)
        }
        val newFrameCount = (scene.frameCount - count).coerceAtLeast(1)
        val newStart = scene.playbackRange.first.coerceAtMost(newFrameCount-1)
        val newEnd = scene.playbackRange.last.coerceAtMost(newFrameCount-1).coerceAtLeast(newStart)
        return scene.copy(layers=newLayers, frameCount=newFrameCount, playbackRange=newStart..newEnd)
    }
    fun extendExposure(layer: Layer, frame: Int, extra: Int): Layer = layer // hold is implicit via celAt; no-op structural
}

