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
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(sum(trees, other.trees, null))

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
    fun top(): T = trees.fold((trees.iterator().next() as BinomialTree<T>).value) { acc, tree -> minOf(acc, tree!!.value) }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        TODO()
    }

    private fun sum(
        left: FList<BinomialTree<T>?>,
        right: FList<BinomialTree<T>?>,
        carry: BinomialTree<T>?
    ): FList<BinomialTree<T>?> {
        if (left is FList.Nil && right is FList.Nil) {
            return if (carry == null) FList.Nil() else FList.Cons(carry, FList.Nil())
        }

        fun carryPlus(left: BinomialTree<T>?, right: BinomialTree<T>?, carry: BinomialTree<T>?):
                Pair<BinomialTree<T>?, BinomialTree<T>?> {
            return if (left == null) {
                if (right == null) Pair(carry, null) else if (carry == null) Pair(right, null)
                else Pair(null, right + carry)
            } else if (right == null) {
                if (carry == null) Pair(left, null) else Pair(null, left + carry)
            } else Pair(carry, left + right)
        }

        val result = carryPlus(
            if (left is FList.Cons) left.head else null,
            if (right is FList.Cons) right.head else null,
            carry
        )
        return FList.Cons(
            result.first, sum(
                if (left is FList.Cons) left.tail else FList.nil(),
                if (right is FList.Cons) right.tail else FList.nil(),
                result.second
            )
        )
    }
}
