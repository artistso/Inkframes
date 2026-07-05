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

typealias StrokePoint = StrokeNode

data class StrokeData(
    val brushId: String = "ink",
    val color: RgbaColor = RgbaColor.BLACK,
    val points: List<StrokeNode> = emptyList()
) {
    // secondary constructor matching legacy call order (points first) – not needed now
    constructor(points: List<StrokeNode>, brushId: String = "ink", color: RgbaColor = RgbaColor.BLACK) : this(brushId, color, points)
}
