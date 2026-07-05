package com.inkframe.core.model

import com.inkframe.core.common.*
import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// MediaTypes
enum class DocumentKind(val mimeType: String, val extension: String) {
    PROJECT("application/vnd.inkframe.project", "inkframe"),
    MP4("video/mp4", "mp4"),
    GIF("image/gif", "gif"),
    PNG_SEQUENCE("application/zip", "zip");
    val mime get() = mimeType
}
object MediaTypes {
    val PROJECT_OPEN_MIME_TYPES = arrayOf(
        "application/vnd.inkframe.project",
        "application/zip",
        "application/octet-stream",
        "*/*"
    )
    fun suggestedFileName(base: String, kind: DocumentKind): String {
        val safe = sanitizeBaseName(base)
        return "$safe.${kind.extension}"
    }
    fun sanitizeBaseName(s: String): String {
        var out = s.trim().replace(Regex("[^A-Za-z0-9._-]+"), "_")
        out = out.replace(Regex("^_+|_+$"), "")
        if (out.isEmpty()) return "Untitled"
        return if (out.length > 80) out.substring(0,80) else out
    }
    fun extensionOf(name: String): String? {
        val idx = name.lastIndexOf('.')
        if (idx < 0 || idx == name.length-1) return null
        return name.substring(idx+1).lowercase()
    }
    fun isProjectFileName(name: String): Boolean {
        return extensionOf(name) == DocumentKind.PROJECT.extension
    }
}

