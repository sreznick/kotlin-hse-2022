package binomial

interface SelfMergeable<T> {
    operator fun plus(other: T): T
}

/*
 * BinomialTree - реализация биномиального дерева
 *
 * Вспомогательная структура для биномиальной кучи
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
 * Дерево совсем без элементов не предусмотрено
 */

class BinomialTree<T : Comparable<T>> private constructor(val value: T, val children: FList<BinomialTree<T>>) :
    SelfMergeable<BinomialTree<T>> {
    // порядок дерева
    val order: Int = children.size

    /*
     * слияние деревьев
     * При попытке слить деревья разных порядков, нужно бросить IllegalArgumentException
     *
     * Требуемая сложность - O(1)
     */
    override fun plus(other: BinomialTree<T>) = plus(this, other)

    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialTree<T> = BinomialTree(value, FList.nil())

        private fun <T : Comparable<T>> plus(b1: BinomialTree<T>, b2: BinomialTree<T>): BinomialTree<T> {
            if (b1.order != b2.order) {
                throw IllegalArgumentException()
            }
            if (b1.value <= b2.value) {
                return BinomialTree(b1.value, FList.Cons(b2, b1.children))
            } else {
                return plus(b2, b1);
            }
        }
    }
}
