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
 * Детали внутренней реализации должны быть спрятаны
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
class BinomialHeap<T: Comparable<T>>
private constructor(private val trees: FList<out BinomialTree<T>?>) : SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T: Comparable<T>> single(value: T) = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    private fun mergeTreesOrNull(a: BinomialTree<T>?, b: BinomialTree<T>?) = when {
        a == null -> b
        b == null -> a
        else -> a + b
    }

    private fun <U> isNull(a: U?) = if (a == null) 1 else 0

    private fun recursiveMerge(tr1: FList<out BinomialTree<T>?>, tr2: FList<out BinomialTree<T>?>, ans: BinomialTree<T>?)
            : FList<BinomialTree<T>?> {
        if (tr1.isEmpty && tr2.isEmpty && ans == null)
            return FList.Nil()
        val a = tr1.headOrNull()
        val b = tr2.headOrNull()
        return when (isNull(a) * 4 + isNull(b) * 2 + isNull(ans)) {
            2, 5 -> FList.Cons(b, recursiveMerge(tr1.tail(), tr2.tail(), mergeTreesOrNull(a, ans)))
            3, 4 -> FList.Cons(a, recursiveMerge(tr1.tail(), tr2.tail(), mergeTreesOrNull(b, ans)))
            else -> FList.Cons(ans, recursiveMerge(tr1.tail(), tr2.tail(), mergeTreesOrNull(a, b)))
        }
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>) = BinomialHeap(recursiveMerge(trees, other.trees, null))

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T) = plus(single(elem))

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top() = trees.filter { it != null }.minOf { it!!.value }

    /*
     * удаление минимального элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> = dropRecur(top(), trees)

    private fun dropRecur(min: T, tr: FList<out BinomialTree<T>?>,
                          f1: FList<BinomialTree<T>?> = FList.Nil(), f2: FList<BinomialTree<T>> = FList.Nil())
            : BinomialHeap<T> {
        if (tr.isEmpty)
            return BinomialHeap(f1.reverse()) + BinomialHeap(f2.reverse())
        val head = tr.headOrNull()
        if (head?.value != min)
            return dropRecur(min, tr.tail(), FList.Cons(head, f1), f2)
        return dropRecur(min, tr.tail(), FList.Cons(null, f1), head.children)
    }
}
