package parsers

import Failure
import Left
import Location
import Right
import Success
import toError

interface StringParsers {
    fun string(s: String): Parser<String, String> = { input: Location<String> ->

        fun String.equalsFromIndex(other: String, startsFrom: Int): Boolean {
            if (this.length - startsFrom < other.length) return false
            for ((index, c) in other.withIndex()) if (this[startsFrom + index] != c) return false
            return true
        }

        if (input.input.equalsFromIndex(s, input.offset)) {
            Right(Success(input.input.substring(input.offset, input.offset + s.length), s.length))
        } else Left(Failure(input.toError("Expected: $s")))
    }
}