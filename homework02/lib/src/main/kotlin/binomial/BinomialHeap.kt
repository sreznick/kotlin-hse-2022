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
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> =
            BinomialHeap(FList.Cons(BinomialTree.single(value), FList.nil()))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        return BinomialHeap(plusWithCarry(this.trees, other.trees))
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
        val result: BinomialTree<T> = trees.fold(null) { acc: BinomialTree<T>?, v ->
            if (acc == null) v
            else if (v == null) acc
            else if (acc.value <= v.value) acc
            else v
        } ?: throw NoSuchElementException("Heap is empty. There is no minimum")
        return result.value
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minValue = top()
        val treeWithMinValue: BinomialTree<T> =
            (trees.filter { it != null && it.value == minValue } as FList.Cons).head
                ?: throw NoSuchElementException("Heap is empty. There is no minimum")

        return BinomialHeap(trees.map { if (it == treeWithMinValue) null else it })
            .plus(BinomialHeap(treeWithMinValue.children.reverse().map { it }))
    }

    private fun plusWithCarry(
        lhs: FList<BinomialTree<T>?>, rhs: FList<BinomialTree<T>?>, carry: BinomialTree<T>? = null
    ): FList<BinomialTree<T>?> {

        if (lhs == FList.nil && rhs == FList.nil && carry == null) return FList.nil()

        val lhsTree = if (lhs == FList.nil) null else (lhs as FList.Cons).head
        val rhsTree = if (rhs == FList.nil) null else (rhs as FList.Cons).head

        val lhsTail = if (lhs == FList.nil) lhs else (lhs as FList.Cons).tail
        val rhsTail = if (rhs == FList.nil) rhs else (rhs as FList.Cons).tail

        val leftResult =
            if (lhsTree != null && rhsTree != null && carry != null
                || lhsTree == null && rhsTree == null && carry != null
            ) carry
            else if (lhsTree != null && rhsTree == null && carry == null) lhsTree
            else if (lhsTree == null && rhsTree != null && carry == null) rhsTree
            else null
        val nextCarry =
            if (lhsTree != null && rhsTree != null) lhsTree.plus(rhsTree)
            else if (lhsTree != null && carry != null) lhsTree.plus(carry)
            else if (rhsTree != null && carry != null) rhsTree.plus(carry)
            else null

        return FList.Cons(leftResult, plusWithCarry(lhsTail, rhsTail, nextCarry))
    }
}

