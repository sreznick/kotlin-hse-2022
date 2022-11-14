package binomial

import kotlin.math.max

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
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))

    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */


    private class MergeIterator<T : Comparable<T>>(
        val it1: Iterator<BinomialTree<T>?>, val it2: Iterator<BinomialTree<T>?>)
        : Iterator<Pair<BinomialTree<T>?, BinomialTree<T>?>> {
        override fun hasNext(): Boolean {
            return it1.hasNext() || it2.hasNext()
        }

        override fun next(): Pair<BinomialTree<T>?, BinomialTree<T>?> {
            val first = if (it1.hasNext())  it1.next() else null
            val second = if (it2.hasNext()) it2.next() else null
            return Pair(first, second)
        }
    }
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> {
        val newChildren: FList<BinomialTree<T>?> = FList.Nil()
        val base: BinomialTree<T>? = null
        val pair: Pair<BinomialTree<T>?, FList<BinomialTree<T>?>> = Pair(base, newChildren)
        val elements =
        MergeIterator(this.trees.iterator(), other.trees.iterator()).asSequence()
            .fold(pair) { prevAndList, elem ->
                val left = elem.first
                val right = elem.second
                val prev = prevAndList.first
                val list = prevAndList.second
                left?.run {
                    right?.run {
                        Pair(left.plus(right), FList.Cons(prev, list))
                    } ?: prev?.run {
                        Pair(
                            left.plus(prev),
                            FList.Cons(null, list)
                        )
                    } ?: Pair(null, FList.Cons(left, list))
                } ?: run {
                    if (right != null && prev != null) {
                        Pair(right.plus(prev), FList.Cons(null, list))
                    } else {
                        Pair(null, FList.Cons(right ?: prev, list))
                    }
                }

            }.let {
                it.first?.run {FList.Cons(it.first, it.second)} ?: it.second
            }
        return BinomialHeap(elements.reverse())
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> {
        return this.run {this.plus(BinomialHeap.single(elem)) }
    }

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T {
        return trees.asSequence()
            .filterNotNull()
            .minOf{tree -> tree.value}
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = trees.filterNotNull()
            .minOfWith({tree1, tree2 -> tree1.value.compareTo(tree2.value)}) {tree -> tree}
        val resultList: FList<BinomialTree<T>?> = FList.nil()
        return BinomialHeap(trees.fold(resultList) { list, elem ->
            if (elem === minTree) FList.Cons(null, list) else FList.Cons(elem, list)
        }
            .dropWhile { it === null }
            .reverse())
            .plus(BinomialHeap(minTree.children.reverse().map { it }))
    }
}

