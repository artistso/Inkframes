package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.asDouble

fun JsonValue.asFloat(): Float = asDouble().toFloat()
