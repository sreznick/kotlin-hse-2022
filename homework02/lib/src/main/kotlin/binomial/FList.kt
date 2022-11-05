package binomial

import java.lang.IndexOutOfBoundsException
import java.lang.RuntimeException

/*
 * FList - реализация функционального списка
 *
 * Пустому списку соответствует тип Nil, непустому - Cons
 *
 * Запрещено использовать
 *
 *  - var
 *  - циклы
 *  - стандартные коллекции
 *
 *  Исключение Array-параметр в функции flistOf. Но даже в ней нельзя использовать цикл и forEach.
 *  Только обращение по индексу
 */
sealed class FList<T>: Iterable<T> {
    // размер списка, 0 для Nil, количество элементов в цепочке для Cons
    abstract val size: Int
    // пустой ли списк, true для Nil, false для Cons
    abstract val isEmpty: Boolean

    // получить список, применив преобразование
    // требуемая сложность - O(n)
    abstract fun <U> map(f: (T) -> U): FList<U>

    // получить список из элементов, для которых f возвращает true
    // требуемая сложность - O(n)
    abstract fun filter(f: (T) -> Boolean): FList<T>

    // свертка
    // требуемая сложность - O(n)
    // Для каждого элемента списка (curr) вызываем f(acc, curr),
    // где acc - это base для начального элемента, или результат вызова
    // f(acc, curr) для предыдущего
    // Результатом fold является результат последнего вызова f(acc, curr)
    // или base, если список пуст
    abstract fun <U> fold(base: U, f: (U, T) -> U): U

    abstract fun foldFirst(f: (T, T) -> T): T

    abstract operator fun plus(other: FList<T>): FList<T>

    // Если i < 0 результат не определён
    abstract operator fun get(i: Int): T

    abstract fun getOrNull(i: Int): T?

    abstract fun dropOrEmpty(i: Int): FList<T>

    // разворот списка
    // требуемая сложность - O(n)
    fun reverse(): FList<T> = fold<FList<T>>(nil()) { acc, current ->
        Cons(current, acc)
    }

    abstract fun findAndReplace(value: T, f: (T) -> Boolean): Pair<FList<T>, T?>

    /*
     * Это не очень красиво, что мы заводим отдельный Nil на каждый тип
     * И вообще лучше, чтобы Nil был объектом
     *
     * Но для этого нужны приседания с ковариантностью
     *
     * dummy - костыль для того, что бы все Nil-значения были равны
     *         и чтобы Kotlin-компилятор был счастлив (он требует, чтобы у Data-классов
     *         были свойство)
     *
     * Также для борьбы с бойлерплейтом были введены функция и свойство nil в компаньоне
     */
    data class Nil<T>(private val dummy: Int = 0) : FList<T>() {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun hasNext() = false

            override fun next() = throw RuntimeException("Access to nil iterator")
        }

        override val size = 0
        override val isEmpty = true

        override fun <U> map(f: (T) -> U) = Nil<U>()

        override fun filter(f: (T) -> Boolean) = Nil<T>()

        override fun <U> fold(base: U, f: (U, T) -> U) = base

        override fun foldFirst(f: (T, T) -> T): T = throw RuntimeException("No first element to fold")

        override operator fun plus(other: FList<T>): FList<T> = other

        override fun get(i: Int) = throw IndexOutOfBoundsException("Past-the-end access")

        override fun getOrNull(i: Int): T? = null

        override fun dropOrEmpty(i: Int) = Nil<T>()

        override fun findAndReplace(value: T, f: (T) -> Boolean): Pair<FList<T>, T?> = Pair(Nil(), null)
    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            var node: FList<T> = this@Cons

            override fun hasNext() = node !is Nil<*>

            override fun next() = (node as Cons<T>).head.also { node = (node as Cons<T>).tail }
        }

        override val size = 1 + tail.size
        override val isEmpty = false

        override fun <U> map(f: (T) -> U): FList<U> = Cons(f(head), tail.map(f))

        override fun filter(f: (T) -> Boolean): FList<T> = if (f(head)) Cons(head, tail.filter(f)) else tail.filter(f)

        override fun <U> fold(base: U, f: (U, T) -> U) = tail.fold(f(base, head), f)

        override fun foldFirst(f: (T, T) -> T): T = tail.fold(head, f)

        override operator fun plus(other: FList<T>): FList<T> = Cons(head, tail + other)

        override fun get(i: Int) = if (i == 0) head else tail[i - 1]

        override fun getOrNull(i: Int): T? = if (i == 0) head else tail.getOrNull(i - 1)

        override fun dropOrEmpty(i: Int) = if (i == 0) this else tail.dropOrEmpty(i - 1)

        override fun findAndReplace(value: T, f: (T) -> Boolean): Pair<FList<T>, T?> {
            return if (f(head)) {
                Pair(Cons(value, tail), head)
            } else {
                val res = tail.findAndReplace(value, f)
                return Pair(Cons(head, res.first), res.second)
            }
        }
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)
fun <T> flistOf(vararg values: T): FList<T> {
    fun recur(i: Int): FList<T> = if (i == values.size) FList.Nil() else FList.Cons(values[i], recur(i + 1))
    return recur(0)
}
