package parsers

import Failure
import Left
import Location
import Right
import Success
import toError

interface CharParsers {
    fun char(expected: Char): Parser<String, String> = { input: Location<String> ->
        when {
            input.offset >= input.input.length -> Left(Failure(input.toError("Unexpected end")))
            (input.input[input.offset] == expected) -> Right(Success(expected.toString(), 1))
            else -> Left(Failure(input.toError("Expected: $expected")))
        }
    }

    fun chars(expected: String): Parser<String, String> = { input ->
        when {
            input.offset >= input.input.length -> Left(Failure(input.toError("Unexpected end")))
            input.offset >= input.input.length -> Left(Failure(input.toError("Unexpected end")))
            (input.input[input.offset] in expected) -> Right(Success(input.input[input.offset].toString(), 1))
            else -> Left(Failure(input.toError("Expected: $expected")))
        }
    }

    fun charRange(from: Char, to: Char): Parser<String, String> = { input: Location<String> ->
        when {
            input.input.isEmpty() -> Left(Failure(input.toError("Empty string")))
            input.offset >= input.input.length -> Left(Failure(input.toError("Unexpected end")))
            input.input[input.offset] in from..to -> Right(Success(input.input[input.offset].toString(), 1))
            else -> Left(Failure(input.toError("Expected: $from - $to")))
        }
    }

    fun charExcept(chars: String): Parser<String, String> = { input: Location<String> ->
        when {
            input.input.isEmpty() -> Left(Failure(input.toError("Empty string")))
            input.offset >= input.input.length -> Left(Failure(input.toError("Unexpected end")))
            input.input[input.offset] !in chars -> Right(Success(input.input[input.offset].toString(), 1))
            else -> Left(Failure(input.toError("Expected any char except $chars")))
        }
    }
}