package homework03

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString

object CsvWriter {
    suspend fun write(text: String, path: String, file: String) {
        val fileVfs = localVfs(path)
        fileVfs[file].delete()
        val newfile = fileVfs[file].open(VfsOpenMode.CREATE)
        newfile.writeString(text);
        newfile.close()
    }
}