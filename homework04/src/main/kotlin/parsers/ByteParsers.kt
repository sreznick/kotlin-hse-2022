package parsers

import Failure
import Left
import Location
import Right
import Success
import toError

interface ByteParsers {
    fun <T : Any> byte(expected: Byte, convert: (Byte) -> T): Parser<T, ByteArray> = { input: Location<ByteArray> ->
        if (input.input.size <= input.offset) {
            Left(Failure(input.toError("Unexpected end")))
        } else if (input.input[input.offset] == expected) {
            Right(Success(convert(expected), 1))
        } else Left(Failure(input.toError("Expected: $expected")))
    }

    fun <T : Any> byteRange(from: Byte, to: Byte, convert: (Byte) -> T): Parser<T, ByteArray> =
        { input: Location<ByteArray> ->
            when {
                input.input.size <= input.offset -> Left(Failure(input.toError("Unexpected end")))
                input.input[input.offset] in from..to -> Right(Success(convert(input.input[input.offset]), 1))
                else -> Left(Failure(input.toError("Expected: $from - $to, got: ${input.input[input.offset]}")))
            }
        }

    fun <T : Any> unsignedByteRange(from: UByte, to: UByte, convert: (Byte) -> T): Parser<T, ByteArray> =
        { input: Location<ByteArray> ->
            when {
                input.input.size <= input.offset -> Left(Failure(input.toError("Unexpected end")))
                input.input[input.offset].toUByte() in from..to -> Right(Success(convert(input.input[input.offset]), 1))
                else -> Left(Failure(input.toError("Expected: $from - $to")))
            }
        }
}