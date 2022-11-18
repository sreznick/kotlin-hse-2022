package binomial

sealed class FList<T>: Iterable<T> {
    abstract val size: Int
    abstract val isEmpty: Boolean

    abstract fun <U> map(f: (T) -> U): FList<U>

    abstract fun filter(f: (T) -> Boolean): FList<T>

    abstract fun <U> fold(base: U, f: (U, T) -> U): U

    abstract fun head(): T?

    abstract fun tail(): FList<T>

    fun reverse(): FList<T> = fold<FList<T>>(nil()) { acc, current ->
        Cons(current, acc)
    }

    data class Nil<T>(private val dummy: Int=0) : FList<T>() {
        override val size: Int
            get() = 0
        override val isEmpty: Boolean
            get() = true

        override fun head(): T? {
            return null
        }

        override fun tail(): FList<T> {
            return flistOf()
        }

        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun hasNext(): Boolean {
                return false;
            }

            override fun next(): Nothing {
                throw java.util.NoSuchElementException("There are no elements")
            }
        }

        override fun <U> map(f: (T) -> U): FList<U> {
            return nil();
        }
        override fun <U> fold(base: U, f: (U, T) -> U): U {
            return base;
        }

        override fun filter(f: (T) -> Boolean): FList<T> {
            return nil();
        }
    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {
        override val size: Int
            get() = tail.size + 1
        override val isEmpty: Boolean
            get() = false

        override fun head(): T? {
            return head
        }

        override fun tail(): FList<T> {
            return tail
        }

        override fun iterator(): Iterator<T> = object : Iterator<T> {
            var cons: FList<T> = this@Cons
            override fun hasNext(): Boolean {
                return !cons.isEmpty
            }

            override fun next(): T {
                val element = (cons as Cons).head
                cons = (cons as Cons).tail
                return element
            }
        }

        override fun <U> map(f: (T) -> U): FList<U> {
            return Cons(f(head), tail.map(f))
        }

        override fun <U> fold(base: U, f: (U, T) -> U): U {
            return tail.fold(f(base, head), f);
        }

        override fun filter(f: (T) -> Boolean): FList<T> {
            if (f(head)) {
                return Cons(head, tail.filter(f))
            }
            return tail.filter(f)
        }
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

fun <T> flistOf(vararg values: T): FList<T> {
    if (values.isEmpty()) {
        return FList.nil()
    }
    return FList.Cons(values[0], flistOf(*values.sliceArray(1 until values.size)))
}
