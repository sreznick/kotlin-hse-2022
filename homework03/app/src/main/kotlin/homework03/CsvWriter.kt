package homework03

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString


object CsvWriter {
    suspend fun write(csv: String, filename: String) {
        val path = System.getProperty("user.dir").plus("/files")
        val fileVfs = localVfs(path)
        fileVfs[filename].delete()
        val file = fileVfs[filename].open(VfsOpenMode.CREATE)
        file.writeString(csv)
        file.close()
    }
}