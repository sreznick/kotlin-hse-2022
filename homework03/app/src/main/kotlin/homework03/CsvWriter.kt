package homework03

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString

object CsvWriter {
    suspend fun writeCsv(path: String, filename: String, csv: String) {
        localVfs(path)[filename].open(VfsOpenMode.CREATE).writeString(csv)
    }
}
