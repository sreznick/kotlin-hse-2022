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
class BinomialHeap<T : Comparable<T>> private constructor(public val trees: FList<BinomialTree<T>?>) :
    SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    fun findByOrder(order: Int): BinomialTree<T>? =
        trees.fold(null) { acc: BinomialTree<T>?, currentTree: BinomialTree<T>? ->
            if (currentTree?.order == order) currentTree else acc
        }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        val thisOrder: Int = (trees as? FList.Cons)?.head?.order ?: -1
        val otherOrder: Int = (other.trees as? FList.Cons)?.head?.order ?: -1
        if (thisOrder == -1 && otherOrder == -1) {
            throw IllegalArgumentException("You can't plus empty heaps")
        } else if (thisOrder == -1) {
            return other;
        } else if (otherOrder == -1) {
            return this
        }
        if (thisOrder < otherOrder) {
            return other.plus(this)
        }
        return BinomialHeap(
            trees.reverse()
                .fold(Pair(flistOf(), 0)) { acc: Pair<FList<BinomialTree<T>?>, Int>, currentTree: BinomialTree<T>? ->
                    val currentOrder: Int = currentTree?.order ?: throw IllegalArgumentException("Heap can't be empty")

//                  Определяем, если у нас случай 3 - II
                    val necessaryTree: BinomialTree<T>? = other.findByOrder(currentOrder)

//                  Определяем, если у нас случай 2 - II
                    val otherTail: FList<BinomialTree<T>?> = other.trees.filter { currentOtherTree: BinomialTree<T>? ->
                        currentOtherTree?.order in (acc.second until currentOrder)
                    }

                    val accBuildTree: FList<BinomialTree<T>?> = acc.first

                    val accActualTail: FList<BinomialTree<T>?>

                    val firstBinomialTree: BinomialTree<T>?

                    if (accBuildTree is FList.Cons<BinomialTree<T>?>) {

//                      Определяем, есть ли у нас случай 1 - II
                        if (accBuildTree.head?.order == currentOrder && otherTail is FList.Nil) {
                            accActualTail =
                                accBuildTree.tail // не доделано, а если там other есть? - нет, по по условиям того, что firstBinomalTree есть в случае отсутсвия treeOrder
                            firstBinomialTree = accBuildTree.head
                        } else {
                            if (otherTail is FList.Cons) {
                                val bufAcc: FList.Cons<BinomialTree<T>?> =
                                    (BinomialHeap(otherTail) + BinomialHeap(accBuildTree)).trees as FList.Cons
                                if (bufAcc.head?.order == currentOrder) {
                                    firstBinomialTree = bufAcc.head
                                    accActualTail = bufAcc.tail
                                } else {
                                    firstBinomialTree = null
                                    accActualTail = bufAcc
                                }
                            } else {
                                accActualTail = accBuildTree
                                firstBinomialTree = null
                            }
                        }
                    } else {
                        accActualTail = otherTail
                        firstBinomialTree = null
                    }

                    if (firstBinomialTree == null && necessaryTree == null) {
                        Pair(FList.Cons(currentTree, accActualTail), currentOrder + 1)
                    } else if (firstBinomialTree != null && necessaryTree == null) {
                        val mergedTree = firstBinomialTree + currentTree
                        Pair(FList.Cons(mergedTree, accActualTail), currentOrder + 1)
                    } else if (firstBinomialTree == null && necessaryTree != null) {
                        val mergedTree = currentTree + necessaryTree
                        Pair(FList.Cons(mergedTree, accActualTail), currentOrder + 1)
                    } else { // firstBinomialTree != null && necessaryTree != null
                        val mergedTree = currentTree + (necessaryTree as BinomialTree<T>)
                        Pair(FList.Cons(mergedTree, FList.Cons(firstBinomialTree, accActualTail)), currentOrder + 1)
                    }
                }.first
        )
    }


    /*
     * добавление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = plus(BinomialHeap(flistOf(BinomialTree.single(elem))))

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T = trees.fold(
        (trees as? FList.Cons)?.head?.value ?: throw IllegalArgumentException("Heap must have at least 1 vertex")
    )
    { acc: T, currentTree: BinomialTree<T>? ->
        val currentTreeValue: T =
            currentTree?.value ?: throw IllegalArgumentException("Heap must have at least 1 vertex")
        if (acc < currentTreeValue) acc else currentTreeValue
    }

    /*
     * удаление элементIllegal argumentsа
     *
     * Требуемая сложность - O(log(n))
     */

    private fun getPairWithMainPartOfBinomialTreeAndWithTreeConsistsDetermineOrder(order: T): Pair<FList<BinomialTree<T>?>, BinomialTree<T>?> =
        trees.fold(
            Pair(
                FList.nil(),
                null
            )
        ) { acc: Pair<FList<BinomialTree<T>?>, BinomialTree<T>?>, currentTree: BinomialTree<T>? ->
            val currentMinValue: T = currentTree?.value ?: throw IllegalArgumentException("BinaryTree can't be empty")
            return@fold if (order == currentMinValue) Pair(acc.first, currentTree) else Pair(
                FList.Cons(
                    currentTree,
                    acc.first
                ), acc.second
            )
        }

    fun drop(): BinomialHeap<T> {
        val splitHeap = getPairWithMainPartOfBinomialTreeAndWithTreeConsistsDetermineOrder(top())
        val childrenOfDroppedTree: FList<BinomialTree<T>> =
            splitHeap.second?.children ?: throw IllegalArgumentException("You can't delete last element or Empty Heap")
        return BinomialHeap(splitHeap.first.reverse()) + BinomialHeap(childrenOfDroppedTree.map { tree: BinomialTree<T> -> tree })
    }
}

