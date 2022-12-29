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
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> =
            BinomialHeap(FList.Cons(BinomialTree.single(value), FList.nil()))
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> =
        BinomialHeap(mergeImpl(null,null, null, trees.iterator(), other.trees.iterator()))

    private fun mergeImpl(mergedTree: BinomialTree<T>?, iter1tree: BinomialTree<T>?, iter2tree: BinomialTree<T>?,
                          iter1: Iterator<BinomialTree<T>?>, iter2: Iterator<BinomialTree<T>?>): FList<BinomialTree<T>?> {

        val tree1: BinomialTree<T>? = if (iter1tree == null && iter1.hasNext()) getTree(iter1) else iter1tree
        val tree2: BinomialTree<T>? = if (iter2tree == null && iter2.hasNext()) getTree(iter2) else iter2tree

        if (treeRelation(tree1, tree2) == TreeRelation.NULL) {
            return if (treeRelation(tree1, mergedTree) == TreeRelation.NULL) FList.nil()
            else FList.Cons(mergedTree, FList.nil())
        }

        if (treeRelation(mergedTree, tree1) == TreeRelation.LESS && treeRelation(mergedTree, tree2) == TreeRelation.LESS) {
            return FList.Cons(mergedTree, mergeImpl(null, tree1, tree2, iter1, iter2))
        } else if (treeRelation(mergedTree, tree1) == TreeRelation.EQUAL) {
            return mergeImpl(tree1!! + mergedTree!!, null, tree2, iter1, iter2)
        } else if (treeRelation(mergedTree, tree2) == TreeRelation.EQUAL) {
            return mergeImpl(tree2!! + mergedTree!!, tree1, null, iter1, iter2)
        }

        if (treeRelation(tree1, tree2) == TreeRelation.EQUAL) {
            return mergeImpl(tree1!! + tree2!!, null, null, iter1, iter2)
        } else if (treeRelation(tree1, tree2) == TreeRelation.LESS) {
            return FList.Cons(tree1, mergeImpl(mergedTree, null, tree2, iter1, iter2))
        } else {
            return FList.Cons(tree2, mergeImpl(mergedTree, tree1, null, iter1, iter2))
        }
    }

    private enum class TreeRelation {
        LESS, MORE, EQUAL, NULL
    }

    private fun treeRelation(tree1: BinomialTree<T>?, tree2: BinomialTree<T>?): TreeRelation {
        return if (tree1 == null && tree2 == null) TreeRelation.NULL
        else if (tree1 == null) TreeRelation.MORE
        else if (tree2 == null) TreeRelation.LESS
        else if (tree1.order < tree2.order) TreeRelation.LESS
        else if (tree1.order == tree2.order) TreeRelation.EQUAL
        else TreeRelation.MORE
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
    fun top(): T = getValue(minTree())

    private fun getTree(iter: Iterator<BinomialTree<T>?>): BinomialTree<T> = iter.next() ?: throw IllegalArgumentException()

    private fun getValue(tree: BinomialTree<T>?): T = tree?.value ?: throw IllegalArgumentException()

    private fun minTree(): BinomialTree<T>? = trees.fold(getTree(trees.iterator()))
        { a: BinomialTree<T>?, b: BinomialTree<T>? -> if (getValue(a) < getValue(b)) a else b }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val droppedTree = minTree()
        val savedTrees = trees.filter { tree: BinomialTree<T>? -> tree != droppedTree }
        return droppedTree?.children?.fold(BinomialHeap(savedTrees)) { heap: BinomialHeap<T>, tree: BinomialTree<T> ->
            heap + BinomialHeap(flistOf(tree)) } ?: throw IllegalArgumentException()
    }
}

