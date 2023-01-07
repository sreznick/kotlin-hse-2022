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
class BinomialHeap<T : Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>) : SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    private fun plusRealisation(first: FList<BinomialTree<T>?>, second: FList<BinomialTree<T>?>, buf: BinomialTree<T>?): FList<BinomialTree<T>?> {
        if (first == FList.nil && second == FList.nil && buf == null) return FList.nil()
        val firstHead = if (first == FList.nil) null else (first as FList.Cons).head
        val secondHead = if (second == FList.nil) null else (second as FList.Cons).head
        val firstTail = if (first == FList.nil) first else (first as FList.Cons).tail
        val secondTail = if (second == FList.nil) second else (second as FList.Cons).tail

        if (firstHead == null && secondHead == null) {
            if (buf != null) return FList.Cons(buf, plusRealisation(firstTail, secondTail, null))
            return FList.Cons(null, plusRealisation(firstTail, secondTail, null))
        }
        if (firstHead == null && secondHead != null) {
            if (buf != null) return FList.Cons(null, plusRealisation(firstTail, secondTail, secondHead.plus(buf)))
            return FList.Cons(secondHead, plusRealisation(firstTail, secondTail, null))
        }
        if (firstHead != null && secondHead == null) {
            if (buf != null) return FList.Cons(null, plusRealisation(firstTail, secondTail, firstHead.plus(buf)))
            return FList.Cons(firstHead, plusRealisation(firstTail, secondTail, null))
        }
        if (firstHead != null && secondHead != null) {
            if  (buf != null) return FList.Cons(buf, plusRealisation(firstTail, secondTail, firstHead.plus(secondHead)))
            return FList.Cons(null, plusRealisation(firstTail, secondTail, firstHead.plus(secondHead)))
        }
        return FList.Cons(null, plusRealisation(firstTail, secondTail, null))

    }
    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>) = BinomialHeap(plusRealisation(this.trees, other.trees, null))

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
    fun top(): T {
        return trees
            .map { it?.value }
            .filter { it != null }
            .fold<T?>(null) { acc, v -> if (acc == null || (v != null && v < acc)) v else acc }
            ?: throw NoSuchElementException("no elements")
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = trees.fold(null) { acc: BinomialTree<T>?, v -> if (acc == null || (v != null && v.value < acc.value)) v else acc }
            ?: throw NoSuchElementException("no elements")
        return BinomialHeap(trees.map { if (it != minTree) it else null }).plus(BinomialHeap(minTree.children.reverse().map { it }))
    }
}