package parsers

import Failure
import Left
import Location
import Right
import Success
import Result
import toError

interface Combinators {


    fun <T : Any, B : Any> or(some: Parser<T, B>, other: Parser<T, B>): Parser<T, B> =
        { input: Location<B> ->
            val someResult = some(input)

            when (someResult) {
                is Left -> {
                    val otherResult = other(input)
                    otherResult
                }
                is Right -> {
                    someResult
                }
            }
        }


    // applies f to result if it's correct
    fun <T : Any, S : Any, ResT : Any, B : Any> seq(
        first: Parser<out T, B>,
        second: Parser<out S, B>,
        merge: (T, S) -> ResT,
        f: ((ResT) -> Unit)? = null
    ): Parser<ResT, B> = { input: Location<B> ->
        val firstValue = first(input)

        when (firstValue) {
            is Left -> {
                firstValue
            }
            is Right -> {
                when (val secondValue = second(Location(input.input, input.offset + firstValue.value.consumed))) {
                    is Left -> {
                        secondValue
                    }
                    is Right -> {
                        val ans = merge(firstValue.value.a, secondValue.value.a)
                        f?.invoke(ans)
                        Right(
                            Success(
                                ans,
                                firstValue.value.consumed + secondValue.value.consumed
                            )
                        )

                    }
                }
            }
        }
    }

    fun <ResT : Any, B : Any> readLengthThenData(
        first: Parser<Int, B>,
        second: (Int) -> Parser<ResT, B>,
        f: ((ResT) -> Unit)? = null
    ): Parser<ResT, B> = { input: Location<B> ->
        val firstValue = first(input)

        when (firstValue) {
            is Left -> {
                firstValue
            }
            is Right -> {

                val secondValue =
                    second(firstValue.value.a)(Location(input.input, input.offset + firstValue.value.consumed))
                when (secondValue) {
                    is Left -> {
                        secondValue
                    }
                    is Right -> {
                        val ans = secondValue.value.a
                        f?.invoke(ans)
                        Right(
                            Success(
                                ans,
                                firstValue.value.consumed + secondValue.value.consumed
                            )
                        )

                    }
                }

            }
        }
    }

    fun <T : Any, B : Any> seqList(vararg parsers: Parser<T, B>, merge: (T, T) -> T): Parser<T, B> {
        return parsers.reduce { acc, parser -> seq(acc, parser, merge) }
    }

    fun <T : Any, B : Any> seqList(parsers: List<Parser<T, B>>, merge: (T, T) -> T): Parser<T, B> {
        return parsers.reduce { acc, parser ->
            seq(acc, parser, merge)
        }
    }


    fun <T : Any, B : Any> plus(parser: Parser<T, B>, merge: (T, T) -> T): Parser<T, B> = { input: Location<B> ->
        fun rec(res: Parser<T, B>): Parser<T, B> {
            val parser2 = seq(res, parser, merge)
            val value = parser2(input)
            return when (value) {
                is Left -> res
                is Right -> rec(parser2)
            }
        }
        rec(parser)(input)
    }

    fun <T : Any, B : Any> star(
        parser: Parser<T, B>,
        neutral: T,
        merge: (T, T) -> T
    ): Parser<T, B> = { input: Location<B> ->

        val res = plus(parser, merge)(input)
        when (res) {
            is Left -> Right(Success(neutral, 0))
            is Right -> res
        }
    }


    fun <T : Any, B : Any> starUpTo(
        parser: Parser<T, B>,
        amount: Int,
        neutral: T,
        merge: (T, T) -> T
    ): Parser<T, B> = { input: Location<B> ->
        fun rec(res: Parser<T, B>): Parser<T, B> {
            val parser2 = seq(res, parser, merge)
            val value = parser2(input)
            when (value) {
                is Left -> return res
                is Right -> {
                    return if (value.value.consumed > amount) res
                    else rec(parser2)
                }
            }
        }
        if (amount == 0) Right(Success(neutral, 0))
        else {
            val res = rec(parser)(input)
            when (res) {
                is Left -> Right(Success(neutral, 0))
                is Right -> res
            }
        }
    }

    fun <T : Any, B : Any> skipN(n: Int, neutral: T, getSize: (B) -> Int): Parser<T, B> = { input: Location<B> ->
        if (getSize(input.input) < input.offset + n) Left(
            Failure(
                input.toError(
                    "Unexpected end: ${getSize(input.input)} < ${input.offset + n}"
                )
            )
        )
        else Right(Success(neutral, n))
    }

    fun <T : Any, B : Any> skip(parser: Parser<T, B>, neutral: T): Parser<T, B> =
        star(parser, neutral) { a, b -> neutral }


    fun <T : Any, B : Any> emptyParser(neutral: T): Parser<T, B> = { input: Location<B> ->
        Right(Success(neutral, 0))
    }

    fun <T : Any, B : Any> maybe(parser: Parser<T, B>, neutral: T): Parser<T, B> = { input: Location<B> ->
        val res = parser(input)
        when (res) {
            is Right -> res
            is Left -> Right(Success(neutral, 0))
        }
    }

    fun <T : Any, B : Any> ignore(parser: Parser<out Any, B>, neutral: T): Parser<T, B> = { input: Location<B> ->
        val res = parser(input)
        when (res) {
            is Left -> res
            is Right -> Right(Success(neutral, res.value.consumed))
        }
    }

    fun <T : Any, B : Any, C : Any> map(parser: Parser<C, B>, f: (C) -> T) = { input: Location<B> ->
        val res = parser(input)
        when (res) {
            is Left -> res
            is Right -> Right(Success(f(res.value.a), res.value.consumed))
        }
    }
}

