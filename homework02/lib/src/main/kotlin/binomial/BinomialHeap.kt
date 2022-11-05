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
class BinomialHeap<T: Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>): SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(mergeLists(trees, other.trees, null))

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = this + single(elem)

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T = trees.filter { it != null }.map { it!!.value }.foldFirst(::minOf)

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minValue = top()
        val node = trees.findAndReplace(null) { it != null && it.value == minValue }
        return BinomialHeap(mergeLists(node.first, node.second!!.children.reverse().map { it }, null))
    }

    private fun mergeLists(lst1: FList<BinomialTree<T>?>,
                           lst2: FList<BinomialTree<T>?>,
                           carry: BinomialTree<T>?): FList<BinomialTree<T>?> {
        if (lst1.isEmpty && lst2.isEmpty) return if (carry == null) FList.Nil() else flistOf(carry)

        val h1 = lst1.getOrNull(0)
        val h2 = lst2.getOrNull(0)
        if (h1 != null && h2 != null && carry != null) {
            return FList.Cons(carry, mergeLists(lst1.dropOrEmpty(1), lst2.dropOrEmpty(1), h1 + h2))
        }
        val treeSum = h1 + h2 + carry
        val nonEmptyTrees = flistOf(h1, h2, carry).filter { it != null }.size
        return if (nonEmptyTrees == 2) {
            FList.Cons(null, mergeLists(lst1.dropOrEmpty(1), lst2.dropOrEmpty(1), treeSum))
        } else {
            FList.Cons(treeSum, mergeLists(lst1.dropOrEmpty(1), lst2.dropOrEmpty(1), null))
        }
    }
}

