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
class BinomialHeap<T : Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>) :
// Наверно, предполагается что у нас на месте нулей в записи двоичного числа, соответствующего куче стоит null,
//  а на месте единиц - соответствующие деревья, null не может быть последним элементом
    SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    override fun toString(): String {
        return trees.toString()
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> {
        val it1 = this.trees.iterator()
        val it2 = other.trees.iterator()
        // Если два дерева одного порядка, то просто мерджим их как деревья
        // Иначе - Cons списка с деревом меньшего порядка и всего остального

        fun nullableTreeSum(
            tree1: BinomialTree<T>?,
            tree2: BinomialTree<T>?,
            tree3: BinomialTree<T>? = null
        ): Pair<BinomialTree<T>?, BinomialTree<T>?> =
            when {
                tree1 == null && tree2 == null && tree3 != null -> tree3 to null
                tree1 == null && tree2 != null && tree3 == null -> tree2 to null
                tree1 == null && tree2 != null && tree3 != null -> null to tree2 + tree3
                tree1 != null && tree2 == null && tree3 == null -> tree1 to null
                tree1 != null && tree2 == null && tree3 != null -> null to tree1 + tree3
                tree1 != null && tree2 != null && tree3 == null -> null to tree1 + tree2
                tree1 != null && tree2 != null && tree3 != null -> tree1 to tree2 + tree3
                else -> null to null
            }


        fun merge(
            element1: BinomialTree<T>?,
            element2: BinomialTree<T>?,
            element3: BinomialTree<T>?,
            it1: Iterator<BinomialTree<T>?>,
            it2: Iterator<BinomialTree<T>?>
        ): FList<BinomialTree<T>?> {

            fun mergeSingle(
                element1: BinomialTree<T>?,
                element2: BinomialTree<T>?,
                it: Iterator<BinomialTree<T>?>
            ): FList<BinomialTree<T>?> {
                val treeSum = nullableTreeSum(element1, element2)
                return if (!it.hasNext()) {
                    if (treeSum.second == null) flistOf(treeSum.first)
                    else flistOf(treeSum.first, treeSum.second)
                } else FList.Cons(treeSum.first, mergeSingle(treeSum.second, it.next(), it))
            }

            val treeSum = nullableTreeSum(element1, element2, element3)
            return when {
                it1.hasNext() && it2.hasNext() -> FList.Cons(
                    treeSum.first,
                    merge(it1.next(), it2.next(), treeSum.second, it1, it2)
                )
                it1.hasNext() && !it2.hasNext() -> FList.Cons(
                    treeSum.first,
                    mergeSingle(treeSum.second, it1.next(), it1)
                )
                !it1.hasNext() && it2.hasNext() -> FList.Cons(
                    treeSum.first,
                    mergeSingle(treeSum.second, it2.next(), it2)
                )
                /* !it1.hasNext() && !it2.hasNext() */
                else -> FList.Cons(
                    treeSum.first,
                    flistOf(treeSum.second)
                )
            }

        }

        return BinomialHeap(merge(it1.next(), it2.next(), null, it1, it2))
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
        return trees.filter { it != null }.map { it!!.value }.minOrNull() ?: throw EmptyHeapException()
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val treeWithMinElement =
            trees.filter { it != null }.minByOrNull { it!!.value } ?: throw EmptyHeapException()
        val minElement = treeWithMinElement.value
        val heap1 = BinomialHeap(trees.map { if (it?.value == minElement) null else it })
        return if (treeWithMinElement.order == 0) heap1
        else {
            val heap2 = BinomialHeap(treeWithMinElement.children.map { if (it.order < 0) null else it }.reverse())
            heap1.plus(heap2)
        }
    }
}

class EmptyHeapException : RuntimeException("Empty heap is not supported")
