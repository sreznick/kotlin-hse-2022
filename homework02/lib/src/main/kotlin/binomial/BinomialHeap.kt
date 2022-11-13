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

    private fun merge(long: FList<BinomialTree<T>?>, short: FList<BinomialTree<T>?>, shift: BinomialTree<T>?): FList<BinomialTree<T>?> {
        if (short.isEmpty) {
            return if (shift == null) long
            else if (long.isEmpty) FList.Cons(shift, long)
            else if ((long as FList.Cons).head == null) FList.Cons(shift, merge(long.tail, short, null))
            else FList.Cons(null, merge(long.tail, short, long.head?.plus(shift)))
        }
        val longCons = long as FList.Cons
        val shortCons = short as FList.Cons
        if (longCons.head == null) {
            return if (shortCons.head == null) FList.Cons(shift, merge(longCons.tail, shortCons.tail,null))
                    else if (shift == null) FList.Cons(shortCons.head, merge(longCons.tail, shortCons.tail,null))
                    else FList.Cons(null, merge(longCons.tail, shortCons.tail,shift.plus(shortCons.head)))
        } else if (shortCons.head == null) {
            return if (shift == null) FList.Cons(longCons.head, merge(longCons.tail, shortCons.tail, null))
                    else FList.Cons(null, merge(longCons.tail, shortCons.tail,shift.plus(longCons.head)))
        } else if (shift == null) {
            return FList.Cons(null, merge(longCons.tail, shortCons.tail,shortCons.head.plus(longCons.head)))
        } else {
            return FList.Cons(shift, merge(longCons.tail, shortCons.tail,shortCons.head.plus(longCons.head)))
        }
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> =
        BinomialHeap(if (trees.size > other.trees.size) merge(trees, other.trees, null)
        else merge(other.trees, trees, null))


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
        val max = trees.firstNotNullOf { it }.value
        return trees.minOf { tree: BinomialTree<T>? -> tree?.value ?:  max}
    }

    private fun buildList(source: FList<BinomialTree<T>>, i: Int) : FList<BinomialTree<T>?> {
        if (source.isEmpty) {
            return FList.nil()
        }
        val consSource = source as FList.Cons
        if (i < consSource.head.order) {
            return FList.Cons(null, buildList(consSource, i + 1))
        } else {
            return FList.Cons(consSource.head, buildList(consSource.tail, i + 1))
        }
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val min = top()
        val tree = trees.filter { it != null && it.value == min }.first()
        val newHeap = BinomialHeap(buildList(tree!!.children.reverse(), 0))
        val erasedHeap = BinomialHeap(trees.map { if (it == tree) null else it })
        return erasedHeap.plus(newHeap)
    }
}

