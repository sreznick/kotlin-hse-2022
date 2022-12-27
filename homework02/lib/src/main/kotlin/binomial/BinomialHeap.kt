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

    private fun merge(
        left: FList<BinomialTree<T>?>,
        right: FList<BinomialTree<T>?>,
        carry: BinomialTree<T>?
    ): FList<BinomialTree<T>?> {
        if (left.size == 0 && right.size == 0 && carry == null) {
            return flistOf()
        }

        val leftHead = if (left is FList.Cons) left.head else null
        val rightHead = if (right is FList.Cons) right.head else null

        val sumInNode = when {
            (leftHead == null && rightHead == null) -> null to null
            (leftHead == null) -> null to rightHead
            (rightHead == null) -> null to leftHead
            else -> leftHead.plus(rightHead) to null
        }
        val leftTail = if (left is FList.Cons) left.tail else flistOf()
        val rightTail = if (right is FList.Cons) right.tail else flistOf()

        return if (sumInNode.first != null) {
            FList.Cons(carry, merge(leftTail, rightTail, sumInNode.first))
        } else {
            val snd = sumInNode.second
            val sum = when {
                (snd == null && carry == null) -> null to null
                (snd == null) -> null to carry
                (carry == null) -> null to snd
                else -> snd.plus(carry) to null
            }
            FList.Cons(sum.second, merge(leftTail, rightTail, sum.first))
        }
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(merge(trees, other.trees, null))

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
    fun top(): T = trees.minOf { tree: BinomialTree<T>? -> tree?.value ?: trees.firstNotNullOf { it }.value }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = trees.filter { it != null && it.value == top() }.first()!!
        return BinomialHeap(trees.map { if (it != minTree) it else null }) +
            BinomialHeap(minTree.children.reverse() as FList<BinomialTree<T>?>)
    }
}

