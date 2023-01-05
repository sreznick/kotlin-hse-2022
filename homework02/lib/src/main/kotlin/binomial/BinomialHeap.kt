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
class BinomialHeap<T: Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>>): SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    private fun mergeTrees(trees1: FList<BinomialTree<T>>, trees2: FList<BinomialTree<T>>) : FList<BinomialTree<T>> {
        if (trees1.isEmpty && trees2.isEmpty)
            return FList.nil()
        if (trees1.isEmpty)
            return trees2
        if (trees2.isEmpty)
            return trees1
        val order1 = trees1.head?.order
        val order2 = trees2.head?.order
        return if (order1!! > order2!!)
            FList.Cons(trees2.head!!, mergeTrees(trees2.tail, trees1))
        else if (order1 < order2)
            FList.Cons(trees1.head!!, mergeTrees(trees1.tail, trees2))
        else
            mergeSame(trees1.head!! + trees2.head!!, mergeTrees(trees1.tail, trees2.tail))
    }

    private fun mergeSame(head: BinomialTree<T>, tail: FList<BinomialTree<T>>): FList<BinomialTree<T>> {
        if (!tail.isEmpty && head.order == tail.head!!.order)
            return mergeSame(head + tail.head!!, tail.tail)
        return FList.Cons(head, tail)
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(mergeTrees(trees, other.trees))

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
    fun top(): T = trees.minOf { it.value }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val el = trees.minOfWith({a, b -> a.value.compareTo(b.value)}, { it })
        return BinomialHeap(trees.filter { it !== el }) + BinomialHeap(el.children.reverse())
    }
}

