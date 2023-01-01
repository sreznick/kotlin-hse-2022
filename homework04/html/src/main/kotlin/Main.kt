import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Expected filename in program args")
        return;
    }

    val filename = args[0]
    val file = File(filename)
    if (!file.isFile || file.isDirectory) {
        println("File not found")
        return;
    }
    val inputString = file.bufferedReader().use { it.readText() }

    val parser = ExtendedParsers().parseBody()

    val result = parser(CharSource(inputString))

    if (result is Right) {
        println("Result HTML:")
        println(result.value)
    } else {
        println("Error parsing file's html:")
        println(result)
    }
}