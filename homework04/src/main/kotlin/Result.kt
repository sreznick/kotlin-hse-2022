import parsers.ParseError

sealed class Result<out A : Any>
data class Success<A : Any>(val a: A, val consumed: Int) : Result<A>()
data class Failure<T: Any>(val get: ParseError<T>) : Result<Nothing>()
