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
            BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        return BinomialHeap(plusImpl(this.trees, other.trees, null))
    }

    private fun plusImpl(
        first: FList<BinomialTree<T>?>,
        second: FList<BinomialTree<T>?>,
        carry: BinomialTree<T>?
    ): FList<BinomialTree<T>?> {
        if (first == FList.nil && second == FList.nil && carry == null)
            return FList.nil()

        val firstHead = if (first == FList.nil) null else (first as FList.Cons).head
        val secondHead = if (second == FList.nil) null else (second as FList.Cons).head
        val firstTail = if (first == FList.nil) first else (first as FList.Cons).tail
        val secondTail = if (second == FList.nil) second else (second as FList.Cons).tail

        if (firstHead != null && secondHead != null && carry != null) {
            return FList.Cons(carry, plusImpl(firstTail, secondTail, firstHead.plus(secondHead)))
        } else if (firstHead == null && secondHead == null && carry != null) {
            return FList.Cons(carry, plusImpl(firstTail, secondTail, null))
        } else if (firstHead == null && secondHead != null && carry == null) {
            return FList.Cons(secondHead, plusImpl(firstTail, secondTail, null))
        } else if (firstHead == null && secondHead != null && carry != null) {
            return FList.Cons(null, plusImpl(firstTail, secondTail, secondHead.plus(carry)))
        } else if (firstHead != null && secondHead == null && carry == null) {
            return FList.Cons(firstHead, plusImpl(firstTail, secondTail, null))
        } else if (firstHead != null && secondHead == null && carry != null) {
            return FList.Cons(null, plusImpl(firstTail, secondTail, firstHead.plus(carry)))
        } else if (firstHead != null && secondHead != null && carry == null) {
            return FList.Cons(null, plusImpl(firstTail, secondTail, firstHead.plus(secondHead)))
        } else {
            return FList.Cons(null, plusImpl(firstTail, secondTail, null))
        }
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
        return trees
            .map { it?.value }
            .filter { it != null }
            .fold<T?>(null) { acc, v -> if (acc == null || (v != null && v < acc)) v else acc }
            ?: throw NoSuchElementException("heap is empty")
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = trees.fold(null)
        { acc: BinomialTree<T>?, v -> if (acc == null || (v != null && v.value < acc.value)) v else acc }
            ?: throw NoSuchElementException("heap is empty")
        return BinomialHeap(trees.map { if (it == minTree) null else it })
            .plus(BinomialHeap(minTree.children.reverse().map { it }))
    }
}

