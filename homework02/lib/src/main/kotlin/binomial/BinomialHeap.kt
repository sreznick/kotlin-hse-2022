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
class BinomialHeap<T : Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>>) :
    SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> =
            BinomialHeap(FList.Cons(BinomialTree.single(value), FList.nil()))
    }


    private fun checkHelper(cur: Int, list: FList<BinomialTree<T>>): Boolean {
        if (list is FList.Nil) {
            return true
        }
        val nl = list as FList.Cons
        assert(cur < nl.head.order)
        return checkHelper(nl.head.order, nl.tail)
    }

    fun check() {
        if (!checkHelper(-1, trees)) {
            throw UnknownError(" Shouldn't be possible ")
        }
    }


    /* Инвариант: в списке детей кучи деревья расположены по возрастанию размера (т.е. next().order > order)
        Однако в самих деревьях наоборот
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(selfMerge(mergeTrees(trees, other.trees)))

    private fun selfMerge(list: FList<BinomialTree<T>>): FList<BinomialTree<T>> {
        if (list.isEmpty || list.tail().isEmpty) {
            return list
        }
        val head = list.head()
        val tail = list.tail()
        val secondTail = tail.tail()
        if (head.order < tail.head().order) {
            return FList.Cons(head, selfMerge(tail))
        } else  {
            if (secondTail.isEmpty || secondTail.head().order != head.order) {
                return selfMerge(FList.Cons(head + tail.head(), secondTail))
            } else {
                return FList.Cons(head, selfMerge(tail))
            }
        }
    }

    private fun mergeTrees(first: FList<BinomialTree<T>>, second: FList<BinomialTree<T>>): FList<BinomialTree<T>> {
        if (first.isEmpty) {
            if (second.isEmpty) {
                return FList.Nil()
            }
            return FList.Cons(second.head(), mergeTrees(first, second.tail()))
        } else if (second.isEmpty) {
            return FList.Cons(first.head(), mergeTrees(first.tail(), second))
        } else {
            if (second.head().order <= first.head().order) {
                return FList.Cons(second.head(), mergeTrees(first, second.tail()))
            } else {
                return FList.Cons(first.head(), mergeTrees(first.tail(), second))
            }
        }
    }

    /*
     * добавление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> {
        return plus(BinomialHeap(flistOf(BinomialTree.single(elem))))
    }

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T = trees.fold(trees.head().value) { acc, tree -> minOf(acc, tree.value) }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val topTree = trees.fold(trees.head()) { acc, tree -> if (tree.value < acc.value) tree else acc }
        return BinomialHeap(trees.delete(topTree)) + BinomialHeap(topTree.children.reverse())
    }
}

