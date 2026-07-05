package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

data class StrokeNode(
    val pos: com.inkframe.core.common.Vec2,
    val pressure: Float = 1f,
    val timeMs: Long = 0L
)

data class StrokeData(
    val points: List<StrokeNode> = emptyList(),
    val brushId: String = "ink",
    val color: RgbaColor = RgbaColor.BLACK
)
