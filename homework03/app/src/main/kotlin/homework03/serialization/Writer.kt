package homework03.serialization

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString

class Writer {
    suspend fun write(csv: String, path: String, filename: String) {
        val fileVfs = localVfs(path)
        fileVfs[filename].delete()
        val file = fileVfs[filename].open(VfsOpenMode.CREATE)
        file.writeString(csv)
        file.close()
    }
}