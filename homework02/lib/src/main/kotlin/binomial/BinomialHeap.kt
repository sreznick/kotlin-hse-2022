package binomial

class BinomialHeap<T: Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>): SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T: Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    override fun plus(other :BinomialHeap<T>): BinomialHeap<T> {
        fun binomialTreesSumWithNullCases(a: BinomialTree<T>?, b: BinomialTree<T>?) : Pair<BinomialTree<T>?, BinomialTree<T>?> {
            val res = when {
                (a == null && b == null) -> Pair(null, null)
                (a == null) -> Pair(null, b)
                (b == null) -> Pair(null, a)
                else -> Pair(a.plus(b), null)
            }
            return res
        }

        fun mergeHeaps(a: FList<BinomialTree<T>?>, b: FList<BinomialTree<T>?>, add: BinomialTree<T>?) : FList<BinomialTree<T>?> {
            if (a.isEmpty && b.isEmpty && add == null) {
                return flistOf()
            }
            val sum = binomialTreesSumWithNullCases(a.head(), b.head());
            val newSum = when {
                (sum.first == null) -> binomialTreesSumWithNullCases(sum.second, add);
                else -> null
            } ?: return FList.Cons(add, mergeHeaps(a.tail(), b.tail(), sum.first))

            return FList.Cons(newSum.second, mergeHeaps(a.tail(), b.tail(), newSum.first))
        }
        return BinomialHeap(mergeHeaps(trees, other.trees, null))
    }

    operator fun plus(elem: T): BinomialHeap<T> = plus(BinomialHeap(flistOf(BinomialTree.single(elem))))

    fun top(): T {
        val minTree = trees.fold(trees.head()) {
                acc, curr ->
                    if ((acc == null) || (curr != null && curr.value < acc.value))
                        curr
                    else
                        acc
        }!!
        return minTree.value
    }

    fun drop(): BinomialHeap<T> {
        val minTree = trees.fold(trees.head()) {
                acc, curr ->
            if ((acc == null) || (curr != null && curr.value < acc.value))
                curr
            else
                acc
        }!!
        return BinomialHeap(trees.map { if (it != minTree) it else null }).plus(BinomialHeap(minTree.children.reverse().map { it }))
    }
}

