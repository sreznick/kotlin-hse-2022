package binomial

import binomial.FList.Companion.nil
import java.lang.RuntimeException
import java.util.*

/*
 * BinomialHeap - реализация биномиальной кучи
 *
 * https://en.wikipedia.org/wiki/Binomial_heap
 *
 * Запрещено использовать
 *
 *  - var
 *  - циклы
 *  - стандартные коллекции
 *
 * Детали внутренней реазации должны быть спрятаны
 * Создание - только через single() и plus()
 *
 * Куча совсем без элементов не предусмотрена
 *
 * Операции
 *
 * plus с кучей
 * plus с элементом
 * top - взятие минимального элемента
 * drop - удаление минимального элемента
 */
class BinomialHeap<T: Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>): SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> =
            BinomialHeap(FList.Cons(BinomialTree.single(value), nil()))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> {
        fun plusRec(a:FList<BinomialTree<T>?>, b:FList<BinomialTree<T>?>, carry:BinomialTree<T>?):FList<BinomialTree<T>?> {
            when (a) {
                is FList.Cons -> when (b) {
                    is FList.Cons -> return if (a.head == null) {
                        if (carry == null) {
                            FList.Cons(b.head, plusRec(a.tail, b.tail, null))
                        } else {
                            plusRec(FList.Cons(carry, a.tail), b, null)
                        }
                    } else if (b.head == null) {
                        plusRec(b, a, carry)
                    } else {
                        FList.Cons(carry, plusRec(a.tail, b.tail,a.head.plus(b.head)))
                    }
                    is FList.Nil -> return if (carry == null) {
                        a
                    } else {
                        plusRec(FList.Cons(carry, nil()), a, null)
                    }
                }
                is FList.Nil -> return when (b) {
                    is FList.Cons -> if (carry == null) {
                        b
                    } else {
                        plusRec(FList.Cons(carry, nil()), b, null)
                    }
                    is FList.Nil -> if (carry == null) {
                        nil()
                    } else {
                        FList.Cons(carry, nil())
                    }
                }
            }
        }
        return BinomialHeap(plusRec(trees, other.trees, null))
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = plus(single(elem))

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T {
        return trees.fold(Optional.empty<T>())
        { acc, cur ->
            if (cur == null) acc
            else if (acc.isEmpty) Optional.of(cur.value)
            else {
                val a = acc.get()
                val b = cur.value
                if (a < b) {
                    Optional.of(a)
                } else {
                    Optional.of(b)
                }
            }
        }.get()
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val m = top()
        val toExtract = trees.filter { it != null && it.value == m }
        val rem = trees.map {
            if (toExtract.contains(it)) null else it
        }
        val trimmed = removeFrontNulls(rem.reverse()).reverse()
        return toExtract.fold(BinomialHeap(trimmed)) {
            acc, cur -> if (cur != null) acc.plus(BinomialHeap(cur.children.reverse().map { it })) else acc
        }
    }

    private fun removeFrontNulls(l : FList<BinomialTree<T>?>) :FList<BinomialTree<T>?> {
        return when (l) {
            is FList.Cons -> if (l.head == null) removeFrontNulls(l.tail) else l
            is FList.Nil -> nil()
        }
    }
}

