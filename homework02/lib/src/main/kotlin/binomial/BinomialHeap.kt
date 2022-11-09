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
class BinomialHeap<T : Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>>) :
    SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(this.trees.merge(other.trees))

    private fun FList<BinomialTree<T>>.merge(other: FList<BinomialTree<T>>): FList<BinomialTree<T>> {
        return if (isEmpty && other.isEmpty) FList.nil()
        else if (isEmpty && !other.isEmpty || !isEmpty && !other.isEmpty && first().order > other.first().order)
            other.merge(this)
        else if (!isEmpty && other.isEmpty || !isEmpty && !other.isEmpty && first().order < other.first().order)
            FList.Cons(first(), (this as FList.Cons).tail.merge(other))
        else compress(first() + other.first(), (this as FList.Cons).tail.merge((other as FList.Cons).tail))
    }

    private fun compress(head: BinomialTree<T>, tail: FList<BinomialTree<T>>): FList<BinomialTree<T>> {
        return if (!tail.isEmpty && head.order == tail.first().order) compress(
            head + tail.first(),
            (tail as FList.Cons).tail
        )
        else FList.Cons(head, tail)
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
    fun top(): T = trees.minOf { it.value }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val element = trees.minOfWith({ a, b -> a.value.compareTo(b.value) }, { it })
        return BinomialHeap(trees.filter { it !== element }) + BinomialHeap(element.children.reverse())
    }
}

