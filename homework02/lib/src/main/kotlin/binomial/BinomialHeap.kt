package binomial

import binomial.FList.Companion.nil

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
class BinomialHeap<T : Comparable<T>> private constructor(val trees: FList<BinomialTree<T>>) :
    SelfMergeable<BinomialHeap<T>> {

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        return BinomialHeap(mergeHaskell(this.trees, other.trees))
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> {
        val list = mergeHaskell(
            FList.Cons(BinomialTree.single(elem), nil()),
            this.trees
        )
        return BinomialHeap(list)
    }

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T {
        val base = (trees as FList.Cons).head
        return trees.fold(base) { v1, v2 -> if (v1.value < v2.value) v1 else v2 }.value
    }

    fun order(): List<Int> {
        val list = mutableListOf<Int>()
        val iter = trees.iterator()
        while (iter.hasNext()) {
            list.add(iter.next().order)
        }
        return list
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        if (trees is FList.Cons) {
            val min = trees.fold(trees.head) { v1, v2 -> if (v1.value < v2.value) v1 else v2 }
            val heapWithRemoved: FList<BinomialTree<T>> = trees.filter { it != min }
            val merged = min.children.fold(heapWithRemoved) { acc, i ->
                mergeHaskell(acc, FList.Cons(i, FList.Nil()))
            }
            return BinomialHeap(merged)
        } else {
            throw IllegalArgumentException()
        }
    }


    companion object {

        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> {
            return BinomialHeap(FList.Cons(BinomialTree.single(value), nil()))
        }

        private fun <T : Comparable<T>> hRank(list: FList<BinomialTree<T>>): Int {
            if (list is FList.Nil) {
                return 0
            } else {
                return (list as FList.Cons).head.order
            }
        }

        //https://hackage.haskell.org/package/TreeStructures-0.0.2/docs/src/Data-Heap-Binomial.html
        private fun <T : Comparable<T>> mergeHaskell(
            b1: FList<BinomialTree<T>>,
            b2: FList<BinomialTree<T>>
        ): FList<BinomialTree<T>> {
            if (b1 is FList.Nil) {
                return b2
            }
            if (b2 is FList.Nil) {
                return b1
            }

            (b1 as FList.Cons)
            (b2 as FList.Cons)

            val h1 = b1.head
            val h2 = b2.head
            val t1 = b1.tail
            val t2 = b2.tail
            if (h1.order == h2.order) {
                val merged = h1.plus(h2)
                if (merged.order != hRank(t1)) {
                    if (merged.order != hRank(t2)) {
                        return FList.Cons(merged, mergeHaskell(t1, t2))
                    } else {
                        return mergeHaskell(FList.Cons(merged, t1), t2)
                    }
                } else {
                    if (merged.order != hRank(t2)) {
                        return mergeHaskell(t1, FList.Cons(merged, t2))
                    } else {
                        return FList.Cons(merged, mergeHaskell(t1, t2))
                    }
                }
            } else if (h1.order < h2.order) {
                return FList.Cons(h1, mergeHaskell(t1, b2))
            } else {
                return FList.Cons(h2, mergeHaskell(t2, b1))
            }
        }
    }
}



