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
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    private fun <T> FList<T?>.head(): T? = if (this is FList.Cons) head else null
    private fun <T> FList<T?>.tail(): FList<T?> = if (this is FList.Cons) tail else flistOf()

    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        fun plusTreesNull(first: BinomialTree<T>?, second: BinomialTree<T>?): Pair<BinomialTree<T>?, BinomialTree<T>?> {
            return if (first == null && second == null) Pair(null, null)
            else if (first == null) Pair(null, second)
            else if (second == null) Pair(null, first)
            else Pair(first.plus(second), null)
        }

        fun merge(
            firstHeap: FList<BinomialTree<T>?>,
            secondHeap: FList<BinomialTree<T>?>,
            shift: BinomialTree<T>?
        ): FList<BinomialTree<T>?> {
            if (firstHeap.isEmpty && secondHeap.isEmpty && shift == null) {
                return flistOf()
            }
            val headPlusHead = plusTreesNull(firstHeap.head(), secondHeap.head())
            return if (headPlusHead.first == null) {
                val sumPlusShift = plusTreesNull(headPlusHead.second, shift)
                FList.Cons(sumPlusShift.second, merge(firstHeap.tail(), secondHeap.tail(), sumPlusShift.first))
            } else {
                FList.Cons(shift, merge(firstHeap.tail(), secondHeap.tail(), headPlusHead.first))
            }
        }
        return BinomialHeap(merge(trees, other.trees, null))
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

    private fun getMinimumTree() =
        trees.fold(trees.head()) { acc, tree -> if ((acc == null) || (tree != null && tree.value < acc.value)) tree else acc }!!
    // одновременно все деревья не null, т.к. у нас не предусмотрена куча без элементов

    fun top(): T = getMinimumTree().value

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minimumTree = getMinimumTree()
        val cutHeap = BinomialHeap(minimumTree.children.reverse().map { it })
        val mainHeap = BinomialHeap(trees.map { if (it != minimumTree) it else null })
        return mainHeap.plus(cutHeap)
    }
}

