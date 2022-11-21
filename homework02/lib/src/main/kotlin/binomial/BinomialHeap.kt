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
        fun mergeTrees(
            res: FList<BinomialTree<T>?>,
            first: FList<BinomialTree<T>?>,
            second: FList<BinomialTree<T>?>
        ): FList<BinomialTree<T>?> {
            if (first.isEmpty && second.isEmpty) {
                return res
            }
            if (first.isEmpty) {
                val secondCons = second as FList.Cons<BinomialTree<T>?>
                return mergeTrees(FList.Cons(secondCons.head, res), first, secondCons.tail)
            }
            if (second.isEmpty) {
                val firstCons = first as FList.Cons<BinomialTree<T>?>
                return mergeTrees(FList.Cons(firstCons.head, res), firstCons.tail, second)
            }
            if (res.isEmpty) {
                val secondCons = second as FList.Cons<BinomialTree<T>?>
                val firstCons = first as FList.Cons<BinomialTree<T>?>
                if (firstCons.head!!.order == secondCons.head!!.order) {
                    return mergeTrees(
                        FList.Cons(firstCons.head + secondCons.head, res),
                        firstCons.tail,
                        secondCons.tail
                    )
                } else if (firstCons.head.order < secondCons.head.order) {
                    return mergeTrees(FList.Cons(firstCons.head, res), firstCons.tail, second)
                } else {
                    return mergeTrees(FList.Cons(secondCons.head, res), first, secondCons.tail)
                }
            }
            val secondCons = second as FList.Cons<BinomialTree<T>?>
            val firstCons = first as FList.Cons<BinomialTree<T>?>
            val resCons = res as FList.Cons<BinomialTree<T>?>
            if (firstCons.head!!.order == secondCons.head!!.order && secondCons.head.order == resCons.head!!.order) {
                return mergeTrees(FList.Cons(firstCons.head + secondCons.head, res), firstCons.tail, secondCons.tail)
            } else if (firstCons.head.order < secondCons.head.order) {
                return mergeTrees(FList.Cons(firstCons.head, res), firstCons.tail, second)
            } else {
                return mergeTrees(FList.Cons(secondCons.head, res), first, secondCons.tail)
            }
        }
        return BinomialHeap(mergeTrees(FList.nil(), trees, other.trees).reverse())

    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> =
        BinomialHeap(FList.Cons(BinomialTree.single(elem), FList.nil())) + this

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T {
        fun helper(res: T, list: FList<BinomialTree<T>?>): T {
            if (list.isEmpty) {
                return res
            }
            val listCons = list as FList.Cons<BinomialTree<T>?>
            if (res < listCons.head!!.value) {
                return helper(res, listCons.tail)
            } else {
                return helper(listCons.head.value, listCons.tail)
            }
        }
        return helper((trees as FList.Cons<BinomialTree<T>?>).head!!.value, trees)
    }

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minimal = top()
        fun helper(value: T, list: FList<BinomialTree<T>?>, tail1: FList<BinomialTree<T>?>): BinomialHeap<T> {
            val listCons = list as FList.Cons<BinomialTree<T>?>
            if (listCons.head!!.value == value) {
                val children = listCons.head.children as FList<BinomialTree<T>?>
                return BinomialHeap(tail1.reverse()) + BinomialHeap(listCons.tail) + BinomialHeap(children)
            } else {
                return helper(value, listCons.tail, FList.Cons(listCons.head, tail1))
            }
        }
        return helper(minimal, trees, FList.nil())
    }
}

