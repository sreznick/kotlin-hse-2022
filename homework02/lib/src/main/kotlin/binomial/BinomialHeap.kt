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
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(FList.Cons(BinomialTree.single(value), FList.nil()))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> {
        val otherHeapOrders = other.trees.map { tree -> tree?.order ?: -1 } //O(log(n))
        val thisHeapOrders = trees.map { tree -> tree?.order ?: -1 } //O(log(n))

        val firstOtherHeapOrder = if (otherHeapOrders.iterator().hasNext()) //O(1)
            otherHeapOrders.iterator().next() else return this
        val firstHeapOrder = if (thisHeapOrders.iterator().hasNext()) //O(1)
            thisHeapOrders.iterator().next() else  return other

        return if (firstOtherHeapOrder == firstHeapOrder) {
            val mergeTrees : BinomialTree<T>? = if(other.trees.iterator().next() != null && trees.iterator().next() != null) { // O(1) - слияние деревьев
                (trees.iterator().next() as BinomialTree<T>) + (other.trees.iterator().next() as BinomialTree<T>)
            } else null
            BinomialHeap(FList.Cons(mergeTrees, FList.nil())) + BinomialHeap((trees as FList.Cons).tail) + BinomialHeap((other.trees as FList.Cons).tail) // O(log(n))
        } else if (firstOtherHeapOrder < firstHeapOrder) {
            BinomialHeap(FList.Cons((other.trees as FList.Cons).head, (this + BinomialHeap(other.trees.tail)).trees)) //O(log(n))
        } else {
            BinomialHeap(FList.Cons((trees as FList.Cons).head, (other + BinomialHeap(this.trees.tail)).trees)) //O(log(n))
        }
    }

    /*
     * добавление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = single(elem) + this

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T {
//        if (!trees.isEmpty)
        return trees.fold((trees.iterator().next() as BinomialTree<T>).value) { it, tree -> if(it < tree?.value as T) it else tree.value }
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minValue = top()
        val treesWithMinValueRoot = trees.filter { it?.value == minValue }
        val heapWithoutTreesWithMinValueRoot = BinomialHeap(trees.filter {it?.value != minValue})
        treesWithMinValueRoot as FList.Cons
        return (treesWithMinValueRoot.head?.children ?: FList.nil()).fold(heapWithoutTreesWithMinValueRoot)
        {acc, current -> BinomialHeap(FList.Cons(current, FList.nil())) + acc } + BinomialHeap(treesWithMinValueRoot.tail)
    }
}