package homework04.parseUtils

interface View<T : Any, A : Any> {
    fun convert(): T
    fun get(index: Int): A
    fun substring(index: Int, count: Int = -1): View<T, A>
    fun startsWith(elem: A): Boolean
    fun isEmpty(): Boolean
    fun size(): Int
}

interface Location<T : Any> {
    val input: T
    val offset: Int
}

sealed class Result<T : Any, A : Any> {
    fun <U : Any, B : Any> merge(other: (Success<T, A>) -> Result<T, B>, mergeResults: (A, B) -> U): Result<T, U> =
        when (this) {
            is Failure -> Failure(this.get)
            is Success -> {
                when (val otherResult = other(this)) {
                    is Failure -> Failure(otherResult.get)
                    is Success -> Success(
                        mergeResults(this.a, otherResult.a),
                        this.consumed + otherResult.consumed
                    )
                }
            }
        }
}
data class Success<T : Any, A : Any>(val a: A, val consumed: Int) : Result<T, A>()
data class Failure<T : Any, A : Any>(val get: ParseError<T>) : Result<T, A>()

data class ParseError<T : Any>(val stack: List<Pair<Location<T>, String>>)

typealias Parser<T, A> = (T) -> Result<T, A>

internal fun <T : Any> Location<T>.toError(msg: String) = ParseError(listOf(this to msg))

interface Parsers {
    fun <T : Any, A : Any> run(p: Parser<T, A>, input: T): Result<T, A> = p(input)
}

open class Combinators : Parsers {

    private fun <T : Any, A : Any> or(some: Parser<T, A>, other: Parser<T, A>): Parser<T, A> = { input: T ->
        when (val someResult = some(input)) {
            is Failure -> {
                when (val otherResult = other(input)) {
                    is Failure -> {
                        Failure(ParseError(someResult.get.stack.plus(otherResult.get.stack)))
                    }
                    is Success -> {
                        otherResult
                    }
                }
            }
            else -> {
                someResult
            }
        }
    }

    @JvmName("infixOr")
    infix fun <T : Any, A : Any> Parser<T, A>.or(other: Parser<T, A>) = or(this, other)

    fun <V : View<T, P>, P : Any, T : Any, A : Any, B : Any, U : Any> seq(
        first: Parser<V, A>,
        second: Parser<V, B>,
        merge: (A, B) -> U
    ): Parser<V, U> =
        { input: V ->

            first(input).merge(
                { firstResult: Success<V, A> -> second(input.substring(firstResult.consumed) as V) },
                merge
            )
        }

    fun <V : View<T, P>, P : Any, T : Any, U : Any> seq(
        vararg parsers: Parser<V, *>,
        merge: (List<Any>) -> U
    ): Parser<V, U> = { input: V ->
        run {
            val results = mutableListOf<Any>()
            var consumed = 0
            for (p in parsers) {
                when (val parsed = run(p as Parser<V, Any>, input.substring(consumed) as V)) {
                    is Failure -> return@run Failure<V, U>(parsed.get)
                    is Success -> {
                        consumed += parsed.consumed
                        results.add(parsed.a)
                    }
                }

            }
            Success(merge(results), consumed)
        }
    }

    fun <V : View<T, P>, P : Any, T : Any, A : Any, B : Any, C : Any> composition(
        first: Parser<V, A>,
        second: (C) -> Parser<V, B>,
        firstResultModifier: (A) -> C,
    ): Parser<V, B> =
        { input: V ->

            first(input).merge(
                { firstResult: Success<V, A> ->
                    run(
                        second(firstResultModifier(firstResult.a)),
                        input.substring(firstResult.consumed) as V
                    )
                },
                { _, b -> b }
            )
        }

    fun <V : View<T, P>, P : Any, T : Any, A : Any> repeatParser(
        p: Parser<V, A>,
        n: Int
    ): Parser<V, List<A>> = { input: V ->
        run {
            val result = mutableListOf<A>()
            var consumed = 0
            repeat(n) {
                when (val parseResult = p(input.substring(consumed) as V)) {
                    is Failure -> return@run Failure(parseResult.get)
                    is Success -> {
                        result.add(parseResult.a)
                        consumed += parseResult.consumed
                    }
                }
            }
            Success(result, consumed)
        }
    }

    fun <V : View<T, P>, P : Any, T : Any, A : Any> repeatUntilFailure(
        p: Parser<V, A>
    ): Parser<V, List<A>> = { input: V ->
        run {
            val result = mutableListOf<A>()
            var consumed = 0
            while (consumed < input.size()) {
                when (val parseResult = p(input.substring(consumed) as V)) {
                    is Failure -> return@run Success(result, consumed)
                    is Success -> {
                        result.add(parseResult.a)
                        consumed += parseResult.consumed
                    }
                }
            }
            Success(result, consumed)
        }
    }

    fun <V : View<T, P>, P : Any, T : Any, A : Any> repeatAtLeastOnceUntilFailure(
        p: Parser<V, A>
    ): Parser<V, List<A>> = { input: V ->
        run {
            val result = mutableListOf<A>()
            var consumed = 0
            while (consumed < input.size()) {
                when (val parseResult = p(input.substring(consumed) as V)) {
                    is Failure -> {
                        if (result.size == 0) return@run Failure(parseResult.get)
                        else return@run Success(result, consumed)
                    }
                    is Success -> {
                        result.add(parseResult.a)
                        consumed += parseResult.consumed
                    }
                }
            }
            Success(result, consumed)
        }
    }

    fun <T : Any, A : Any, U : Any> map(p: Parser<T, A>, resultModifier: (A) -> U): Parser<T, U> = { input: T ->
        when (val result = run(p, input)) {
            is Failure -> Failure(result.get)
            is Success -> Success(resultModifier(result.a), result.consumed)
        }
    }
}