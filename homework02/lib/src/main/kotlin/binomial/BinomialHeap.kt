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
        fun merge (leftTree: FList<BinomialTree<T>?>, rightTree: FList<BinomialTree<T>?>, result: BinomialTree<T>?): FList<BinomialTree<T>?> {
            if (leftTree is FList.Nil) {
                if (rightTree is FList.Nil) {
                    if (result == null) {
                        return FList.nil()
                    } else {
                        return FList.Cons(result, FList.nil())
                    }
                } else {
                    if (result == null) {
                        return rightTree
                    } else {
                        return merge(FList.Cons(result, FList.nil()), rightTree, null)
                    }
                }
            } else if (leftTree is FList.Cons) {
                if (rightTree is FList.Nil) {
                    if (result == null) {
                        return leftTree
                    } else {
                        return merge(FList.Cons(result, FList.nil()), leftTree, null)
                    }
                } else if (rightTree is FList.Cons){
                    if (leftTree.head == null) {
                        if (result == null) {
                            return FList.Cons(rightTree.head, merge(leftTree.tail, rightTree.tail, null))
                        } else {
                            return merge(FList.Cons(result, leftTree.tail), rightTree, null)
                        }
                    } else {
                        if (rightTree.head == null) {
                            return merge(rightTree, leftTree, result)
                        } else {
                            return FList.Cons(result, merge(leftTree.tail, rightTree.tail,leftTree.head.plus(rightTree.head)))
                        }
                    }
                }
            }
            return FList.nil()
        }
        return BinomialHeap(merge(trees, other.trees, null))
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
    fun top(): T = trees.asSequence().filterNotNull().minOf{tree -> tree.value}

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minimal = top()
        val minimalTree: BinomialTree<T> = (trees.filter { it != null && it.value == minimal } as FList.Cons).head ?: throw NoSuchElementException()
        return BinomialHeap(trees.map { if (it == minimalTree) null else it }).plus(BinomialHeap(minimalTree.children.reverse().map { it }))
    }
}

