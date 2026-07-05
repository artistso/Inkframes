package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// LayerOps
object LayerOps {
    fun moveUp(scene: Scene, layerId: String): Scene = move(scene, layerId, +1)
    fun moveDown(scene: Scene, layerId: String): Scene = move(scene, layerId, -1)
    private fun move(scene: Scene, id: String, delta: Int): Scene {
        val idx = scene.layers.indexOfFirst { it.id==id }; if (idx<0) return scene
        val newIdx = (idx+delta).coerceIn(0, scene.layers.size-1); if (newIdx==idx) return scene
        val m = scene.layers.toMutableList(); val l=m.removeAt(idx); m.add(newIdx,l); return scene.copy(layers=m)
    }
    fun moveTo(scene: Scene, layerId: String, toIndex: Int): Scene {
        val idx = scene.layers.indexOfFirst { it.id==layerId }; if (idx<0) return scene
        val m = scene.layers.toMutableList(); val l=m.removeAt(idx); m.add(toIndex.coerceIn(0,m.size), l); return scene.copy(layers=m)
    }
    fun rename(scene: Scene, layerId: String, newName: String): Scene {
        val name = newName.trim().ifEmpty { "Layer" }
        return scene.copy(layers = scene.layers.map { if (it.id==layerId) it.copy(name=name) else it })
    }
    fun delete(scene: Scene, layerId: String): Scene {
        if (scene.layers.size <=1) return scene
        return scene.copy(layers = scene.layers.filterNot { it.id==layerId })
    }
    fun activeAfterDelete(scene: Scene, deletedId: String, oldActive: String): String {
        val idx = scene.layers.indexOfFirst { it.id==deletedId }
        val remaining = scene.layers.filterNot { it.id==deletedId }
        if (remaining.isEmpty()) return oldActive
        val newIdx = idx.coerceAtMost(remaining.size-1).coerceAtLeast(0)
        return remaining[newIdx].id
    }
    fun toggleVisible(layer: Layer) = layer.copy(visible = !layer.visible)
    fun toggleLocked(layer: Layer) = layer.copy(locked = !layer.locked)
    fun setOpacity(layer: Layer, o: Float) = layer.copy(opacity = o.coerceIn(0f,1f))
    fun setBlendMode(layer: Layer, mode: BlendMode) = layer.copy(blendMode=mode)
}

