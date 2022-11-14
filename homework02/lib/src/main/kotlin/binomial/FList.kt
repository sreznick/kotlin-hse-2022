package binomial

import kotlin.NoSuchElementException

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

    data class FListIterator<T>(var list : FList<T>) : Iterator<T> {
        override fun hasNext(): Boolean {
            return !list.isEmpty
        }

        override fun next(): T {
            val result = list.first()
            list = list.tail()
            return result
        }
    }
    override fun iterator(): Iterator<T> {
        return FListIterator(this)
    }

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

    // разворот списка
    // требуемая сложность - O(n)
    fun reverse(): FList<T> = fold<FList<T>>(nil()) { acc, current ->
        Cons(current, acc)
    }

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

    abstract fun first() : T
    abstract fun tail() : FList<T>

    abstract fun dropWhile(f: (T) -> Boolean): FList<T>
    data class Nil<T>(private val dummy: Int=0) : FList<T>() {
        override fun dropWhile(f: (T) -> Boolean): FList<T> = this

        override fun first(): T {
            throw NoSuchElementException("Nil has no element")
        }

        override fun tail(): FList<T> {
            return this
        }

        override val size: Int
            get() = 0
        override val isEmpty: Boolean
            get() = true

        override fun <U> map(f: (T) -> U): FList<U> = nil()

        override fun filter(f: (T) -> Boolean): FList<T> = nil()

        override fun <U> fold(base: U, f: (U, T) -> U): U = base
    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {
        override fun dropWhile(f: (T) -> Boolean): FList<T> = if (f.invoke(head)) tail.dropWhile(f) else this

        override fun first(): T = head;

        override fun tail(): FList<T> = tail

        override val size: Int
            get() = 1 + tail.size
        override val isEmpty: Boolean
            get() = false

        override fun <U> map(f: (T) -> U): FList<U> = Cons(f.invoke(head), tail.map(f))

        override fun filter(f: (T) -> Boolean): FList<T> =
            if (f.invoke(head)) Cons(head, tail.filter(f)) else tail.filter(f)


        override fun <U> fold(base: U, f: (U, T) -> U): U =
            asSequence().fold(base, f)

    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)
fun <T> flistOf(vararg values: T): FList<T> {
    val list : FList<T> = FList.nil()
    if (values.isEmpty()) {
        return list
    }
    return values.foldRight(list) {e, l -> FList.Cons(e, l)}
}


