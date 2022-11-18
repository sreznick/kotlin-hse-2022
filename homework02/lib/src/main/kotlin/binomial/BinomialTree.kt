package binomial

interface SelfMergeable<T> {
    operator fun plus(other: T): T
}

class BinomialTree<T: Comparable<T>> private constructor(val value: T, val children: FList<BinomialTree<T>>): SelfMergeable<BinomialTree<T>> {
    // порядок дерева
    val order: Int = children.size

    override fun plus(other: BinomialTree<T>): BinomialTree<T> {
        if (order != other.order) {
            throw IllegalArgumentException("Trees orders are not equal: $order and ${other.order}")
        }
        if (value > other.value) {
            return BinomialTree(other.value, FList.Cons(this, other.children))
        }
        return BinomialTree(value, FList.Cons(other, children))
    }

    companion object {
        fun <T: Comparable<T>> single(value: T): BinomialTree<T> = BinomialTree(value, FList.nil())
    }
}
