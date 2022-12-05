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
        return BinomialHeap(merge(this.trees, other.trees, null))
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> {
        val list = merge(
            FList.Cons(BinomialTree.single(elem), nil()),
            this.trees,
            null
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
//        var currentT = System.currentTimeMillis()
        if (trees is FList.Cons) {
            var current = trees
            var min = current.head
            while (current is FList.Cons) {
                if (min.value > current.head.value) {
                    min = current.head
                }
                current = current.tail
            }

//            var after = System.currentTimeMillis() - currentT
//            currentT = System.currentTimeMillis()
//            if (after > 25) {
//                throw IllegalArgumentException(this.trees.size.toString())
//            }
            val heapWithRemoved: FList<BinomialTree<T>> = trees.filter { it != min }
//            after = System.currentTimeMillis() - currentT
//            currentT = System.currentTimeMillis()
//            if (after > 35) {
//                throw IllegalArgumentException(this.trees.size.toString())
//            }
            val merged = merge(heapWithRemoved, min.children, null)
//            after = System.currentTimeMillis() - currentT
//            if (after > 10) {
//                throw IllegalArgumentException(this.trees.size.toString())
//            }
            return BinomialHeap(merged)
        } else {
            throw IllegalArgumentException()
        }
    }


    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> {
            return BinomialHeap(FList.Cons(BinomialTree.single(value), nil()))
        }

        private fun <T : Comparable<T>> merge(
            b1: FList<BinomialTree<T>>,
            b2: FList<BinomialTree<T>>,
            curry: BinomialTree<T>?
        ): FList<BinomialTree<T>> {
            if (b1 is FList.Cons && b2 is FList.Cons) {
                val t1 = b1.head
                val t2 = b2.head
                if (t1.order < t2.order) {
                    if (curry != null) {
                        if (curry.order == t1.order) {
                            val curryA = curry.plus(t1)
                            return merge(b1.tail, b2, curryA)
                        } else if (curry.order == t2.order) {
                            val curryA = curry.plus(t2)
                            return merge(b1, b2.tail, curryA)
                        } else {
                            if (curry.order > t1.order) {
                                if (curry.order > t2.order) {
                                    return FList.Cons(
                                        t1,
                                        FList.Cons(t2, FList.Cons(curry, merge(b1.tail, b2.tail, null)))
                                    )
                                } else {
                                    return FList.Cons(
                                        t1,
                                        FList.Cons(curry, FList.Cons(t2, merge(b1.tail, b2.tail, null)))
                                    )
                                }
                            } else {
                                return FList.Cons(curry, FList.Cons(t1, FList.Cons(t2, merge(b1.tail, b2.tail, null))))
                            }
                        }
                    } else {
                        val tail = merge(b1.tail, b2.tail, null)
                        return FList.Cons(t1, FList.Cons(t2, tail))
                    }
                } else if (t1.order > t2.order) {
                    return merge(b2, b1, curry)
                } else {
                    val tail = merge(b1.tail, b2.tail, t1.plus(t2))
                    if (curry != null) {
                        return FList.Cons(curry, tail)
                    } else {
                        return tail
                    }
                }
            } else if (b1 is FList.Nil && b2 is FList.Cons) {
                val t2 = b2.head
                if (curry == null) {
                    return b2
                } else {
                    if (curry.order == t2.order) {
                        return merge(b1, b2.tail, curry.plus(t2))
                    } else {
                        return FList.Cons(curry, b2)
                    }
                }
            } else if (b1 is FList.Cons) {
                return merge(b2, b1, curry)
            } else {
                if (curry == null) {
                    return nil()
                } else {
                    return FList.Cons(curry, nil())
                }
            }
        }
    }
}



