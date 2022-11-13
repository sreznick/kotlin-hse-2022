package binomial

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
class BinomialHeap<T : Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>) :
    SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> {
            return BinomialHeap(FList.Cons(BinomialTree.single(value), FList.nil()))
        }
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        return BinomialHeap(plus(this.trees, other.trees))
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> {
        return BinomialHeap(insertTree(this.trees, BinomialTree.single(elem)))
    }

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T {
        return extractMin(this.trees).first.value
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        return BinomialHeap(extractMin(this.trees).second)
    }
}


private fun <T : Comparable<T>> extractMin(heap: FList<BinomialTree<T>?>): Pair<BinomialTree<T>, FList<BinomialTree<T>?>> {
    if (heap.size == 1) {
        return Pair((heap as FList.Cons).head!!, FList.Nil())
    } else {
        val pairA = extractMin((heap as FList.Cons).tail)
        if (heap.head!!.value < pairA.first.value) {
            return Pair(heap.head, heap.tail)
        } else {
            return Pair(pairA.first, FList.Cons(heap.head, pairA.second))
        }
    }
}

private fun <T : Comparable<T>> insertTree(
    heap: FList<BinomialTree<T>?>,
    tree: BinomialTree<T>
): FList<BinomialTree<T>?> {
    if (heap.isEmpty) {
        return FList.Cons(tree, FList.nil())
    } else {
        if (tree.order < (heap as FList.Cons).head!!.order) {
            return FList.Cons(tree, heap)
        } else {
            return insertTree(heap.tail, heap.head!!.plus(tree))
        }
    }
}

private fun <T : Comparable<T>> plus(
    ts1: FList<BinomialTree<T>?>,
    ts2: FList<BinomialTree<T>?>
): FList<BinomialTree<T>?> {
    if (ts1.isEmpty) {
        return ts2
    }
    if (ts2.isEmpty) {
        return ts1
    }
    if ((ts1 as FList.Cons).head!!.order < (ts2 as FList.Cons).head!!.order) {
        val merged = plus(ts1.tail, ts2)
        return FList.Cons(ts1.head, merged)
    } else if (ts1.head!!.order > ts2.head!!.order) {
        val merged = plus(ts1, ts2.tail)
        return FList.Cons(ts2.head, merged)
    } else {
        return insertTree(plus(ts1.tail, ts2.tail), ts1.head + ts2.head)
    }
}
