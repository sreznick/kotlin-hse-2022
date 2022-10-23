interface DimensionAware {
    val ndim: Int
    fun dim(i: Int): Int {
        if (i < ndim) return unsafeDim(i)
        else throw DimensionAwareException.DimensionAwareIndexOutOfBoundException(i, ndim)
    }

    fun unsafeDim(i: Int): Int
}

sealed class DimensionAwareException(reason: String) : IllegalArgumentException(reason) {
    class DimensionAwareIndexOutOfBoundException(index: Int, bound: Int) :
        DimensionAwareException("Index $index out of bound $bound")
}

interface SizeAware {
    val size: Int
}