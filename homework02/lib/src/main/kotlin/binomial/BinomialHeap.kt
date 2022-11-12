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

    fun <T> FList<T?>.head(): T? = if (this is FList.Cons) head else null

    fun <T> FList<T?>.tail(): FList<T?> = if (this is FList.Cons) tail else flistOf()

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        fun treesSum(t1: BinomialTree<T>?, t2: BinomialTree<T>?): Pair<BinomialTree<T>?, BinomialTree<T>?> =
            when {
                (t1 == null && t2 == null) -> null to null
                (t1 == null) -> null to t2
                (t2 == null) -> null to t1
                else -> t1.plus(t2) to null
            }

        fun recursivePlus(h1: FList<BinomialTree<T>?>, 
                          h2: FList<BinomialTree<T>?>, 
                          carry: BinomialTree<T>? = null): FList<BinomialTree<T>?> {
            if (h1.size == 0 && h2.size == 0 && carry == null) return flistOf()
            val sum = treesSum(h1.head(), h2.head())
            if (sum.first != null) {
                return FList.Cons(carry, recursivePlus(h1.tail(), h2.tail(), sum.first))
            } else {
                val sumWithCarry = treesSum(sum.second, carry)
                return FList.Cons(sumWithCarry.second, recursivePlus(h1.tail(), h2.tail(), sumWithCarry.first))
            }
        }

        return BinomialHeap(recursivePlus(trees, other.trees))
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = plus(single(elem))

    private fun findMinTree(): BinomialTree<T> {
        fun compareTrees(a: BinomialTree<T>?, b: BinomialTree<T>?): BinomialTree<T>? =
            if (a == null || (b != null && b.value < a.value)) b else a

        return (trees as FList.Cons).fold(trees.head, ::compareTrees)!!
    }

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T = findMinTree().value

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = findMinTree()
        val orphansHeap = BinomialHeap(minTree.children.reverse().map { it })
        val remainedHeap = BinomialHeap(trees.map { if (it != minTree) it else null })
        return remainedHeap.plus(orphansHeap)
    }
}

