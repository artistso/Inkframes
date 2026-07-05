package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// ---------- RgbaColor ----------
data class RgbaColor(val r: Float, val g: Float, val b: Float, val a: Float = 1f) {
    init {
        require(r in 0f..1f && g in 0f..1f && b in 0f..1f && a in 0f..1f) { "Color out of range" }
    }
    fun toArgb(): Int {
        val ai = (a.coerceIn(0f,1f)*255f+0.5f).toInt()
        val ri = (r.coerceIn(0f,1f)*255f+0.5f).toInt()
        val gi = (g.coerceIn(0f,1f)*255f+0.5f).toInt()
        val bi = (b.coerceIn(0f,1f)*255f+0.5f).toInt()
        return (ai shl 24) or (ri shl 16) or (gi shl 8) or bi
    }
    fun withAlpha(alpha: Float) = copy(a = alpha.coerceIn(0f,1f))
    companion object {
        val BLACK = RgbaColor(0f,0f,0f,1f)
        val WHITE = RgbaColor(1f,1f,1f,1f)
        val TRANSPARENT = RgbaColor(0f,0f,0f,0f)
        val RED = RgbaColor(1f,0f,0f,1f)
        val GREEN = RgbaColor(0f,1f,0f,1f)
        val BLUE = RgbaColor(0f,0f,1f,1f)
        fun fromArgb(argb: Int): RgbaColor {
            val a = ((argb ushr 24) and 0xFF)/255f
            val r = ((argb ushr 16) and 0xFF)/255f
            val g = ((argb ushr 8) and 0xFF)/255f
            val b = (argb and 0xFF)/255f
            return RgbaColor(r,g,b,a)
        }
    }
    override fun equals(other: Any?): Boolean {
        if (other !is RgbaColor) return false
        return abs(r-other.r)<1e-3f && abs(g-other.g)<1e-3f && abs(b-other.b)<1e-3f && abs(a-other.a)<1e-3f
    }
    override fun hashCode(): Int = toArgb()
}

// ---------- Hsv ----------
data class Hsv(val h: Float, val s: Float, val v: Float, val a: Float = 1f) {
    fun toRgba(): RgbaColor {
        val hh = wrapHue(h)
        val c = v.coerceIn(0f,1f) * s.coerceIn(0f,1f)
        val x = c * (1 - abs((hh/60f)%2 -1))
        val m = v.coerceIn(0f,1f) - c
        val (rp,gp,bp) = when {
            hh < 60 -> Triple(c,x,0f)
            hh < 120 -> Triple(x,c,0f)
            hh < 180 -> Triple(0f,c,x)
            hh < 240 -> Triple(0f,x,c)
            hh < 300 -> Triple(x,0f,c)
            else -> Triple(c,0f,x)
        }
        return RgbaColor((rp+m).coerceIn(0f,1f), (gp+m).coerceIn(0f,1f), (bp+m).coerceIn(0f,1f), a.coerceIn(0f,1f))
    }
    fun withHue(nh: Float) = copy(h = wrapHue(nh))
    fun withSaturation(ns: Float) = copy(s = ns.coerceIn(0f,1f))
    fun withValue(nv: Float) = copy(v = nv.coerceIn(0f,1f))
    fun withAlpha(na: Float) = copy(a = na.coerceIn(0f,1f))
    fun normalized() = Hsv(wrapHue(h), s.coerceIn(0f,1f), v.coerceIn(0f,1f), a.coerceIn(0f,1f))
    companion object {
        fun wrapHue(v: Float): Float { var x = v % 360f; if (x < 0) x += 360f; if (x >= 360f) x -= 360f; return x }
        fun fromRgba(c: RgbaColor): Hsv {
            val r=c.r; val g=c.g; val b=c.b
            val max = maxOf(r,g,b); val min = minOf(r,g,b)
            val delta = max-min
            val v = max
            val s = if (max==0f) 0f else delta/max
            val h = when {
                delta==0f -> 0f
                max==r -> 60f * (((g-b)/delta) % 6)
                max==g -> 60f * (((b-r)/delta)+2)
                else -> 60f * (((r-g)/delta)+4)
            }
            return Hsv(wrapHue(h), s, v, c.a)
        }
    }
}

// ---------- Brush ----------
enum class BrushKind { INK, ROUND, AIRBRUSH }
data class Brush(
    val id: String,
    val name: String,
    val kind: BrushKind = BrushKind.ROUND,
    val sizePx: Float = 24f,
    val minSizePx: Float = 2f,
    val opacity: Float = 1f,
    val flow: Float = 1f,
    val hardness: Float = 0.8f,
    val spacing: Float = 0.15f,
    val smoothing: Float = 0.3f,
    val stabilization: Float = 0f,
    val pressureToSize: Boolean = true,
    val pressureToOpacity: Boolean = true,
    val buildUp: Boolean = false
) {
    fun diameterForPressure(p: Float): Float {
        val pp = p.coerceIn(0f,1f)
        return if (pressureToSize) minSizePx + (sizePx-minSizePx)*pp else sizePx
    }
    fun flowForPressure(p: Float): Float {
        val pp = p.coerceIn(0f,1f)
        return (if (pressureToOpacity) flow*pp else flow).coerceIn(0f,1f)
    }
}

object DefaultBrushes {
    val ink = Brush(id="ink", name="Ink", kind=BrushKind.INK, sizePx=18f, minSizePx=1f, hardness=0.95f, spacing=0.12f, smoothing=0.25f, buildUp=false)
    val round = Brush(id="round", name="Round", kind=BrushKind.ROUND, sizePx=32f, minSizePx=2f, hardness=0.7f, spacing=0.15f, smoothing=0.3f, buildUp=false)
    val airbrush = Brush(id="airbrush", name="Airbrush", kind=BrushKind.AIRBRUSH, sizePx=80f, minSizePx=20f, hardness=0.1f, spacing=0.08f, smoothing=0.4f, flow=0.5f, buildUp=true, pressureToOpacity=true)
    val all = listOf(ink, round, airbrush)
    fun byId(id: String) = all.find { it.id==id }
}

// ---------- BrushAdjustments ----------
object BrushAdjustments {
    val SIZE_RANGE = 1f..512f
    val MIN_SIZE_RANGE = 0f..512f
    val OPACITY_RANGE = 0f..1f
    val FLOW_RANGE = 0f..1f
    val HARDNESS_RANGE = 0f..1f
    val SPACING_RANGE = 0.01f..1f
    val SMOOTHING_RANGE = 0f..0.95f

    fun withSize(b: Brush, v: Float): Brush {
        val nv = v.coerceIn(SIZE_RANGE.start, SIZE_RANGE.endInclusive)
        val nMin = minOf(b.minSizePx, nv)
        return b.copy(sizePx=nv, minSizePx=nMin)
    }
    fun withMinSize(b: Brush, v: Float): Brush {
        val nv = v.coerceIn(MIN_SIZE_RANGE.start, MIN_SIZE_RANGE.endInclusive).coerceAtMost(b.sizePx)
        return b.copy(minSizePx=nv)
    }
    fun withOpacity(b: Brush, v: Float) = b.copy(opacity=v.coerceIn(OPACITY_RANGE.start, OPACITY_RANGE.endInclusive))
    fun withFlow(b: Brush, v: Float) = b.copy(flow=v.coerceIn(FLOW_RANGE.start, FLOW_RANGE.endInclusive))
    fun withHardness(b: Brush, v: Float) = b.copy(hardness=v.coerceIn(HARDNESS_RANGE.start, HARDNESS_RANGE.endInclusive))
    fun withSpacing(b: Brush, v: Float) = b.copy(spacing=v.coerceIn(SPACING_RANGE.start, SPACING_RANGE.endInclusive))
    fun withSmoothing(b: Brush, v: Float) = b.copy(smoothing=v.coerceIn(SMOOTHING_RANGE.start, SMOOTHING_RANGE.endInclusive))
    fun withPressureToSize(b: Brush, v: Boolean) = b.copy(pressureToSize=v)
    fun withPressureToOpacity(b: Brush, v: Boolean) = b.copy(pressureToOpacity=v)
    fun withBuildUp(b: Brush, v: Boolean) = b.copy(buildUp=v)
    fun resetToDefault(b: Brush): Brush = DefaultBrushes.byId(b.id) ?: b
}

// ---------- Canvas / Cel / Layer / Scene / Project ----------
data class CanvasSpec(
    val widthPx: Int,
    val heightPx: Int,
    val fps: Int,
    val pixelAspect: Float = 1f,
    val background: RgbaColor = RgbaColor.WHITE
)

data class CelTransform(
    val tx: Float = 0f,
    val ty: Float = 0f,
    val rotationDeg: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)

data class Cel(
    val id: String = "cel-${System.nanoTime()}",
    val surfaceId: Long,
    val transform: CelTransform = CelTransform()
)

enum class BlendMode(val displayName: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    ADD("Add"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    ERASE("Erase");
    companion object {
        fun fromOrdinalSafe(o: Int): BlendMode = values().getOrElse(o) { NORMAL }
    }
}

data class Layer(
    val id: String = "layer-${(0..999999).random()}",
    val name: String,
    val opacity: Float = 1f,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val cels: Map<Int, Cel> = emptyMap()
) {
    init { require(opacity in 0f..1f) { "opacity out of range" } }
    fun celAt(frame: Int): Cel? {
        if (cels.isEmpty()) return null
        val keys = cels.keys.filter { it <= frame }
        if (keys.isEmpty()) return null
        return cels[keys.maxOrNull()]
    }
}

data class Scene(
    val id: String = "scene-${(0..999999).random()}",
    val name: String,
    val frameCount: Int = 24,
    val layers: List<Layer> = listOf(Layer(name="Layer 1")),
    val playbackRange: IntRange = 0 until frameCount,
    val loop: Boolean = true
) {
    init { require(frameCount >= 1) { "frameCount must >=1" } }
    fun layerById(id: String) = layers.find { it.id == id }
}

data class Project(
    val id: String,
    val name: String,
    val canvas: CanvasSpec,
    val scenes: List<Scene> = emptyList(),
    val activeSceneId: String? = scenes.firstOrNull()?.id,
    val colorPalette: List<RgbaColor> = emptyList(),
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val modifiedAtEpochMs: Long = System.currentTimeMillis()
) {
    val activeScene: Scene? get() = scenes.find { it.id == activeSceneId } ?: scenes.firstOrNull()
}

// ---------- RecentColors ----------
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

// ---------- ColorSampler ----------
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

// ---------- Onion skin ----------
data class OnionSkinSettings(
    val framesBefore: Int = 2,
    val framesAfter: Int = 2,
    val nearOpacity: Float = 0.5f,
    val farOpacity: Float = 0.15f,
    val tintStrength: Float = 0.5f,
    val enabled: Boolean = true,
    val beforeTint: RgbaColor = RgbaColor(1f,0f,0f),
    val afterTint: RgbaColor = RgbaColor(0f,0f,1f)
) {
    init {
        require(framesBefore in 0..MAX_RANGE) { "framesBefore out of range" }
        require(framesAfter in 0..MAX_RANGE) { "framesAfter out of range" }
    }
    companion object {
        const val MAX_RANGE = 8
    }
}
data class OnionGhost(
    val frame: Int,
    val surfaceId: Long,
    val opacity: Float,
    val tint: RgbaColor,
    val tintStrength: Float = 0.5f,
    val offset: Int = 0
)
object OnionSkinPlanner {
    fun plan(currentFrame: Int, settings: OnionSkinSettings, surfaceAt: (Int)->Long?): List<OnionGhost> {
        if (!settings.enabled) return emptyList()
        val out = mutableListOf<OnionGhost>()
        // before
        for (i in 1..settings.framesBefore) {
            val offset = -i
            val f = currentFrame + offset
            val sid = surfaceAt(f) ?: continue
            val t = if (settings.framesBefore <=1) 0f else (i-1).toFloat()/(settings.framesBefore-1)
            val opacity = settings.nearOpacity + (settings.farOpacity - settings.nearOpacity) * t
            out.add(OnionGhost(f, sid, opacity, settings.beforeTint, settings.tintStrength, offset))
        }
        // after
        for (i in 1..settings.framesAfter) {
            val offset = i
            val f = currentFrame + offset
            val sid = surfaceAt(f) ?: continue
            val t = if (settings.framesAfter <=1) 0f else (i-1).toFloat()/(settings.framesAfter-1)
            val opacity = settings.nearOpacity + (settings.farOpacity - settings.nearOpacity) * t
            out.add(OnionGhost(f, sid, opacity, settings.afterTint, settings.tintStrength, offset))
        }
        // farthest first
        return out.sortedByDescending { kotlin.math.abs(it.offset) }
    }
}

// ---------- TimelineOps ----------
object TimelineOps {
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

// ---------- LayerOps ----------
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

// ---------- PlaybackOps ----------
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

// ---------- TimelineDrag ----------
object TimelineDrag {
    fun frameAt(x: Float, cellW: Float, gap: Float, startOffset: Float = 0f): Int {
        val pitch = cellW + gap
        if (pitch <= 0f) return 0
        return ((x - startOffset)/pitch).toInt().coerceAtLeast(0)
    }
    fun resolveDrag(startX: Float, endX: Float, cellW: Float, gap: Float, hasCel: (Int)->Boolean): Pair<Int,Int>? {
        val src = frameAt(startX, cellW, gap)
        val dst = frameAt(endX, cellW, gap)
        if (!hasCel(src)) return null
        return src to dst
    }
}

// ---------- ExportPlanner ----------
data class ExportFrame(val frameIndex: Int, val durationMs: Int)
data class ExportPlan(
    val frames: List<ExportFrame>,
    val fps: Int,
    val widthPx: Int,
    val heightPx: Int,
    val loop: Boolean
) {
    val frameCount get() = frames.size
    val totalDurationMs get() = frames.sumOf { it.durationMs }
}
object ExportPlanner {
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

// ---------- MediaTypes ----------
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

// ---------- ProjectCodec ----------
object ProjectCodec {
    const val FORMAT_VERSION = 1
    fun toJsonString(p: Project): String = toJson(p).toJsonString(pretty=true)
    fun fromJsonString(s: String): Project = fromJson(parseJson(s))
    fun toJson(p: Project): JsonValue = JsonValue.obj(
        "version" to JsonValue.of(FORMAT_VERSION),
        "id" to JsonValue.of(p.id),
        "name" to JsonValue.of(p.name),
        "canvas" to JsonValue.obj(
            "width" to JsonValue.of(p.canvas.widthPx),
            "height" to JsonValue.of(p.canvas.heightPx),
            "fps" to JsonValue.of(p.canvas.fps),
            "pixelAspect" to JsonValue.of(p.canvas.pixelAspect)
        ),
        "scenes" to JsonValue.arr(p.scenes.map { sceneToJson(it) })
    )
    private fun sceneToJson(s: Scene) = JsonValue.obj(
        "id" to JsonValue.of(s.id),
        "name" to JsonValue.of(s.name),
        "frameCount" to JsonValue.of(s.frameCount),
        "playbackRange" to JsonValue.arr(listOf(JsonValue.of(s.playbackRange.first), JsonValue.of(s.playbackRange.last))),
        "loop" to JsonValue.of(s.loop),
        "layers" to JsonValue.arr(s.layers.map { layerToJson(it) })
    )
    private fun layerToJson(l: Layer) = JsonValue.obj(
        "id" to JsonValue.of(l.id),
        "name" to JsonValue.of(l.name),
        "opacity" to JsonValue.of(l.opacity),
        "visible" to JsonValue.of(l.visible),
        "locked" to JsonValue.of(l.locked),
        "blendMode" to JsonValue.of(l.blendMode.name),
        "cels" to JsonValue.arr(l.cels.map { (frame,cel) ->
            JsonValue.obj(
                "frame" to JsonValue.of(frame),
                "id" to JsonValue.of(cel.id),
                "surfaceId" to JsonValue.of(cel.surfaceId.toInt()),
                "transform" to JsonValue.obj(
                    "tx" to JsonValue.of(cel.transform.tx),
                    "ty" to JsonValue.of(cel.transform.ty),
                    "rotationDeg" to JsonValue.of(cel.transform.rotationDeg)
                )
            )
        })
    )
    fun fromJson(v: JsonValue): Project {
        val version = v.optional("version")?.asInt() ?: 1
        require(version <= FORMAT_VERSION) { "Newer format version $version" }
        val id = v["id"].asString()
        val name = v["name"].asString()
        val canvasObj = v["canvas"].asObj()
        val canvas = CanvasSpec(
            widthPx = canvasObj.entries["width"]!!.asInt(),
            heightPx = canvasObj.entries["height"]!!.asInt(),
            fps = canvasObj.entries["fps"]!!.asInt(),
            pixelAspect = canvasObj.entries["pixelAspect"]?.asFloat() ?: 1f
        )
        val scenesArr = v.optional("scenes")?.asArr()?.items ?: emptyList()
        val scenes = scenesArr.map { parseScene(it) }
        return Project(id=id, name=name, canvas=canvas, scenes=scenes, activeSceneId=scenes.firstOrNull()?.id)
    }
    private fun parseScene(v: JsonValue): Scene {
        val o = v.asObj()
        val id = o.entries["id"]!!.asString()
        val name = o.entries["name"]!!.asString()
        val frameCount = o.entries["frameCount"]!!.asInt()
        val playback = o.entries["playbackRange"]?.asArr()
        val range = if (playback!=null) playback.items[0].asInt() .. playback.items[1].asInt() else 0 until frameCount
        val loop = o.entries["loop"]?.asBool() ?: true
        val layers = o.entries["layers"]!!.asArr().items.map { parseLayer(it) }
        return Scene(id=id, name=name, frameCount=frameCount, layers=layers, playbackRange=range, loop=loop)
    }
    private fun parseLayer(v: JsonValue): Layer {
        val o = v.asObj()
        val id = o.entries["id"]?.asString() ?: "layer-${(0..99999).random()}"
        val name = o.entries["name"]!!.asString()
        val opacity = o.entries["opacity"]?.asFloat() ?: 1f
        val visible = o.entries["visible"]?.asBool() ?: true
        val locked = o.entries["locked"]?.asBool() ?: false
        val blendMode = try { BlendMode.valueOf(o.entries["blendMode"]?.asString() ?: "NORMAL") } catch (e: Exception) { BlendMode.NORMAL }
        val celsArr = o.entries["cels"]!!.asArr().items
        val cels = celsArr.associate { celV ->
            val co = celV.asObj()
            val frame = co.entries["frame"]!!.asInt()
            val cid = co.entries["id"]?.asString() ?: "cel-$frame"
            val sid = co.entries["surfaceId"]!!.asLong()
            val tObj = co.entries["transform"]?.asObj()
            val transform = if (tObj!=null) CelTransform(
                tx = tObj.entries["tx"]?.asFloat() ?: 0f,
                ty = tObj.entries["ty"]?.asFloat() ?: 0f,
                rotationDeg = tObj.entries["rotationDeg"]?.asFloat() ?: 0f
            ) else CelTransform()
            frame to Cel(id=cid, surfaceId=sid, transform=transform)
        }
        return Layer(id=id, name=name, opacity=opacity, visible=visible, locked=locked, blendMode=blendMode, cels=cels)
    }
}

// ---------- ProjectPackage ----------
object ProjectPackage {
    const val EXTENSION = "inkframe"
    interface CelImageIO {
        fun encode(surfaceId: Long): ByteArray?
        fun decode(surfaceId: Long, bytes: ByteArray)
    }

    // simple binary format: [jsonLen:Int][jsonBytes][entryCount:Int][ {surfaceId:Long}{dataLen:Int}{data} ... ]
    fun write(project: Project, io: CelImageIO, out: java.io.OutputStream) {
        val json = ProjectCodec.toJsonString(project).toByteArray(Charsets.UTF_8)
        val dos = java.io.DataOutputStream(out)
        dos.writeInt(json.size)
        dos.write(json)
        // collect all surfaceIds
        val sids = project.scenes.flatMap { scene -> scene.layers.flatMap { layer -> layer.cels.values.map { it.surfaceId } } }.distinct()
        var count = 0
        val baos = java.io.ByteArrayOutputStream()
        val tmp = java.io.DataOutputStream(baos)
        for (sid in sids) {
            val data = io.encode(sid) ?: continue
            tmp.writeLong(sid)
            tmp.writeInt(data.size)
            tmp.write(data)
            count++
        }
        tmp.flush()
        dos.writeInt(count)
        dos.write(baos.toByteArray())
        dos.flush()
    }

    fun read(io: CelImageIO, input: java.io.InputStream): Project {
        val dis = java.io.DataInputStream(input)
        val jsonLen = dis.readInt()
        val jsonBytes = ByteArray(jsonLen)
        dis.readFully(jsonBytes)
        val project = ProjectCodec.fromJsonString(jsonBytes.toString(Charsets.UTF_8))
        val count = try { dis.readInt() } catch (e: Exception) { 0 }
        repeat(count) {
            val sid = dis.readLong()
            val len = dis.readInt()
            val data = ByteArray(len)
            dis.readFully(data)
            io.decode(sid, data)
        }
        return project
    }

    // compatibility overloads used by older tests
    fun write(project: Project, io: CelImageIO): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        write(project, io, baos)
        return baos.toByteArray()
    }
    fun read(bytes: ByteArray, io: CelImageIO): Project {
        return read(io, java.io.ByteArrayInputStream(bytes))
    }
}

// top-level alias for tests that import CelImageIO directly
interface CelImageIO : ProjectPackage.CelImageIO

// Helpers
private fun JsonValue.asFloat() = asDouble().toFloat()
