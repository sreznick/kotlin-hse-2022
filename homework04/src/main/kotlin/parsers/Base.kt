package parsers

import Either
import Failure
import Location
import Result
import Success

data class ParseError <out T: Any> (val stack: List<Pair<Location<T>, String>>)

//typealias Parser<A> = (String) -> Either<ParseError, A>
typealias Parser<A, B> = (Location<B>) -> Either<Failure<B>, Success<out A>>

interface Parsers {
    fun <A : Any, B: Any> run(p: Parser<A, B>, input: B): Either<Failure<B>, Success<out A>>
}