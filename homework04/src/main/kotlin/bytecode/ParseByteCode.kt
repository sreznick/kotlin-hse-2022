package bytecode

import Left
import Right
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Some arguments are missing")
        return
    }
    val input = args[0]
    val output = args[1]
    val parsers = ByteCodeParser()
    try {
        val bytes = File(input).readBytes()
        val outputFile = File(output).printWriter()
        outputFile.use {
            when (val result = parsers.run(parsers.parseByteCode(), bytes)) {
                is Right -> outputFile.println(result.value.a)
                is Left -> outputFile.println("${result.value.get.stack[0].first}, ${result.value.get.stack[0].second}")
            }
        }
    } catch (e: FileNotFoundException) {
        println("Can't find file: $input")
    } catch (e: IOException) {
        println("Something went wrong while working with given files")
    }
}
