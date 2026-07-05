package com.inkframe.core.model

import com.inkframe.core.common.JsonValue
import com.inkframe.core.common.parseJson
import kotlin.math.*

// ProjectPackage
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

