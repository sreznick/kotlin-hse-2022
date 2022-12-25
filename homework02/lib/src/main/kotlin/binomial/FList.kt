package binomial

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
    data class Nil<T>(private val dummy: Int = 0) : FList<T>() {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun hasNext(): Boolean = false
            override fun next(): T = Any() as T
        }

        override val size = 0
        override val isEmpty = true

        override fun <U> map(f: (T) -> U): FList<U> = Nil()

        override fun filter(f: (T) -> Boolean): FList<T> = Nil()

        override fun <U> fold(base: U, f: (U, T) -> U): U = base
    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            var current: FList<T> = this@Cons
            override fun hasNext(): Boolean = !current.isEmpty
            override fun next(): T {
                val value = (current as Cons<T>).head
                current = (current as Cons<T>).tail
                return value
            }
        }

        override val size = tail.size + 1
        override val isEmpty = false

        override fun <U> map(f: (T) -> U): FList<U> = Cons(f(head), tail.map(f))

        override fun filter(f: (T) -> Boolean): FList<T> {
            return if (f(head)) {
                Cons(head, tail.filter(f))
            } else {
                tail.filter(f)
            }
        }

        override fun <U> fold(base: U, f: (U, T) -> U): U = tail.fold(f(base, head), f)
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()
    }
}

// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)
fun <T> flistOf(vararg values: T): FList<T> = flistOfByIndex(values, 0)

private fun <T> flistOfByIndex(values: Array<out T>, index: Int): FList<T> {
    return if (index >= values.size) {
        FList.Nil<T>()
    } else {
        FList.Cons<T>(values[index], flistOfByIndex(values, index + 1))
    }
}
