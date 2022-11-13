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
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)));
    }

    fun getTreeOrderNS(tree: BinomialTree<T>?, default: Int): Int = tree?.order ?: default

    fun plusImpl(first: FList<BinomialTree<T>?>, second: FList<BinomialTree<T>?>, overflow: BinomialTree<T>?): FList<BinomialTree<T>?>{
        val firstElem: BinomialTree<T>? = if (!first.isEmpty) first.iterator().next() else null;
        val secondElem: BinomialTree<T>? = if (!second.isEmpty) second.iterator().next() else null;

        val firstElemOrd = getTreeOrderNS(firstElem, -2)
        val secondElemOrd = getTreeOrderNS(secondElem, -1)
        if (firstElemOrd > secondElemOrd){
            return plusImpl(second, first, overflow);
        }

        val overflowElemOrd = getTreeOrderNS(overflow, -3)

        val expectedTreeOrder: Int;

        if (overflowElemOrd >= 0){
            expectedTreeOrder = overflowElemOrd
        } else {
            if (firstElemOrd >= 0){
                expectedTreeOrder = firstElemOrd
            } else {
                expectedTreeOrder = secondElemOrd
            }

        }


        if (expectedTreeOrder < 0){
            return FList.nil()
        }

        if (first.isEmpty && overflowElemOrd < 0){
            if (!second.isEmpty){
                return second;
            }
            return FList.nil();
        }

        val firstMergeVal: BinomialTree<T>?;


        if (overflow == null){
            firstMergeVal = if (firstElemOrd == expectedTreeOrder) firstElem else null;
        } else {
            firstMergeVal = if (firstElemOrd == expectedTreeOrder) overflow.let { firstElem!!.plus(it) } else overflow;
        }



        val firstMergeValOrd = getTreeOrderNS(firstMergeVal, -4)

        if (firstMergeValOrd > expectedTreeOrder){
            val newFirst: FList<BinomialTree<T>?> = (first as FList.Cons<BinomialTree<T>?>).tail;
            if (secondElemOrd == expectedTreeOrder){
                val newSecond = (second as FList.Cons<BinomialTree<T>?>).tail;
                return FList.Cons(secondElem!!, plusImpl(newFirst, newSecond, firstMergeVal))
            } else {
                return plusImpl(newFirst, second, firstMergeVal)
            }
        } else if (firstMergeValOrd == expectedTreeOrder){
            //или заюзали first, или overload
            val newFirst: FList<BinomialTree<T>?>
            if (firstElemOrd == expectedTreeOrder){
                newFirst = (first as FList.Cons<BinomialTree<T>?>).tail;
            } else {
                newFirst = first
            }

            //МОжем попытаться со 2 мерджить
            if (secondElemOrd == expectedTreeOrder){
                val newSecond = (second as FList.Cons<BinomialTree<T>?>).tail;
                return plusImpl(newFirst, newSecond, secondElem!!.plus(firstMergeVal!!))
            } else {
                return FList.Cons(firstMergeVal, plusImpl(newFirst, second, null))
            }
        } else {
            //Тут уже остается вернуть 2
            val newSecond = (second as FList.Cons<BinomialTree<T>?>).tail;
            return FList.Cons(secondElem, plusImpl(first, newSecond, null))
        }
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other :BinomialHeap<T>): BinomialHeap<T>{
        val res = BinomialHeap(plusImpl(trees, other.trees, null).reverse());
        return res
    }

    /*
     * добавление элемента
     * 
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = plus(single(elem));
    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T = trees.fold(trees.iterator().next()!!.value) { e, v -> if (e < v!!.value) e else v.value}


    fun withoutTree(tree: BinomialTree<T>): FList<BinomialTree<T>?> = trees.filter { e -> e != tree }
    fun getTreeWithMin(min: T): BinomialTree<T> = (trees.filter { e -> e!!.value == min } as FList.Cons<BinomialTree<T>>).head;

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        return BinomialHeap(withoutTree(getTreeWithMin(top()))) + BinomialHeap(getTreeWithMin(top()).children.reverse() as FList<BinomialTree<T>?>)
    }
}

