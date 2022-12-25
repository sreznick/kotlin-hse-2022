package binomial

import java.util.InputMismatchException

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
sealed class FList<T> : Iterable<T> {
    // размер списка, 0 для Nil, количество элементов в цепочке для Cons
    abstract val size: Int
    // пустой ли списк, true для Nil, false для Cons
    abstract val isEmpty: Boolean

    abstract fun headOrNull(): T?

    abstract fun tail(): FList<T>

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

    // разворот списка
    // требуемая сложность - O(n)
    fun reverse(): FList<T> = fold<FList<T>>(nil()) { acc, current ->
        Cons(current, acc)
    }

    override fun iterator(): Iterator<T> = FListIterator(this)

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
        override val size = 0
        override val isEmpty = true

        override fun headOrNull() = null

        override fun tail() = Nil<T>()

        override fun <U> fold(base: U, f: (U, T) -> U) = base

        override fun filter(f: (T) -> Boolean) = Nil<T>()

        override fun <U> map(f: (T) -> U) = Nil<U>()
    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {
        override val size = 1 + tail.size
        override val isEmpty = false

        override fun headOrNull() = head

        override fun tail() = tail

        override fun <U> fold(base: U, f: (U, T) -> U) = tail.fold(f(base, head), f)

        override fun filter(f: (T) -> Boolean) = if (f(head)) Cons(head, tail.filter(f)) else tail.filter(f)

        override fun <U> map(f: (T) -> U) = Cons(f(head), tail.map(f))
    }

    private class FListIterator<T>(var fl: FList<T>) : Iterator<T> {
        override fun hasNext() = !fl.isEmpty

        override fun next(): T {
            if (!hasNext())
                throw InputMismatchException()
            val ans = (fl as Cons<T>).head
            fl = (fl as Cons<T>).tail
            return ans
        }
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)

internal fun <T> build(k: Int, vararg values: T): FList<T> =
    if (k == values.size) FList.nil()
    else FList.Cons(values[k], build(k + 1, *values))

fun <T> flistOf(vararg values: T): FList<T> = build(0, *values)
