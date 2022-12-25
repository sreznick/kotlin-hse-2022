sealed class Either<out L : Any, out R : Any> private constructor(left: Any?, right: Any?) {
    abstract fun <T : Any> mapLeft(f: (L) -> T): Left<T>
    abstract fun <T : Any> mapRight(f: (R) -> T): Right<T>

    abstract fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U>
    abstract fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U>
}

data class Left<L : Any>(val value: L) : Either<L, Nothing>(value, null) {
    override fun <T : Any> mapLeft(f: (L) -> T) = Left(f(value))
    override fun <T : Any> mapRight(f: (Nothing) -> T): Nothing = throw IllegalStateException()

    override fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U> =
        Left(f(value, other.value))

    override fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (Nothing, T) -> U): Right<U> =
        throw IllegalStateException()
}

data class Right<R : Any>(val value: R) : Either<Nothing, R>(null, value) {
    override fun <T : Any> mapLeft(f: (Nothing) -> T): Nothing = throw IllegalStateException()
    override fun <T : Any> mapRight(f: (R) -> T) = Right(f(value))

    override fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (Nothing, T) -> U): Left<U> =
        throw IllegalStateException()

    override fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U> =
        Right(f(value, other.value))

}