package binomial

import binomial.FList.Companion.nil

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
        override fun iterator(): Iterator<T> {
            return object : Iterator<T> {
                override fun hasNext(): Boolean {
                    return false
                }

                override fun next(): T {
                    throw IllegalArgumentException()
                }
            }
        }

        override val size: Int
            get() = 0
        override val isEmpty: Boolean
            get() = true

        override fun <U> map(f: (T) -> U): FList<U> {
            return nil()
        }

        override fun filter(f: (T) -> Boolean): FList<T> {
            return nil()
        }

        override fun <U> fold(base: U, f: (U, T) -> U): U {
            return base
        }

    }

    data class Cons<T>(val head: T, val tail: FList<T>) : FList<T>() {

        override fun iterator(): Iterator<T> {

            //Важное уточнение.
            //В итераторе можно var для хранения состояния использовать во второй домашке.
            var current: FList<T> = this

            return object : Iterator<T> {
                override fun hasNext(): Boolean {
                    return current is Cons<T>
                }

                override fun next(): T {
                    if (current is Cons<T>) {
                        val ret = (current as Cons<T>).head
                        current = (current as Cons<T>).tail
                        return ret
                    } else {
                        throw IllegalArgumentException()
                    }
                }
            }
        }

        override val size: Int
            get() = 1 + tail.size
        override val isEmpty: Boolean
            get() = false

        override fun <U> map(f: (T) -> U): FList<U> {
            return map(f, this)
        }

        override fun filter(f: (T) -> Boolean): FList<T> {
            return filter1(f, this)
        }

        override fun <U> fold(base: U, f: (U, T) -> U): U {
            return fold1(base, f, this)
        }
    }

    companion object {
        fun <T> nil() = Nil<T>()
        val nil = Nil<Any>()

        private fun <U, T> fold1(base: U, f: (U, T) -> U, list: FList<T>): U {
            if (list is Cons) {
                return fold1(f(base, list.head), f, list.tail)
            } else {
                return base
            }
        }

        private fun <T> filter1(f: (T) -> Boolean, list: FList<T>): FList<T> {
            if (list is Cons) {
                if (f(list.head)) {
                    return Cons(list.head, filter1(f, list.tail))
                } else {
                    return filter1(f, list.tail)
                }
            } else {
                return nil()
            }
        }

        private fun <U, T> map(f: (T) -> U, list: FList<T>): FList<U> {
            if (list is Cons) {
                return Cons(f(list.head), map(f, list.tail))
            } else {
                return nil()
            }
        }
    }
}

// конструирование функционального списка в порядке следования элементов
// требуемая сложность - O(n)
fun <T> flistOf(vararg values: T): FList<T> {
    return flistOf(values.toList(), 0)
}

private fun <T> flistOf(values: List<T>, index: Int): FList<T> {
    if (index < values.size) {

        return FList.Cons<T>(values.get(index), flistOf(values, index + 1))
    } else {
        return nil()
    }
}
