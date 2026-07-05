package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// RecentColors
data class RecentColors private constructor(val colors: List<RgbaColor>, val capacity: Int) {
    val size get() = colors.size
    fun isEmpty() = colors.isEmpty()
    fun add(c: RgbaColor): RecentColors {
        val cap = if (capacity < 1) 1 else capacity
        val argb = c.toArgb()
        val filtered = colors.filterNot { it.toArgb() == argb }
        val newList = listOf(c) + filtered
        return RecentColors(newList.take(cap), cap)
    }
    companion object {
        fun empty(capacity: Int = 12) = RecentColors(emptyList(), if (capacity < 1) 1 else capacity)
        fun of(list: List<RgbaColor>, capacity: Int = 12) = RecentColors(list.distinctBy { it.toArgb() }.take(if (capacity<1)1 else capacity), if (capacity<1)1 else capacity)
    }
}

