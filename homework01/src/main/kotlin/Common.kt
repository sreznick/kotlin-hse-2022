interface DimensionAware {
    val ndim: Int
    fun dim(i: Int): Int
}

interface SizeAware {
    val size: Int
}

open class DefaultDimensionAware(protected val dimensions: IntArray): DimensionAware {
    override val ndim: Int
        get() = dimensions.size

    override fun dim(i: Int): Int = if (i in 0 until ndim) dimensions[i]
    else throw DimensionAwareArgumentException.NotExistingDimensionException(i, ndim)
}

sealed class DimensionAwareArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class NotExistingDimensionException(dim: Int, ndim: Int) :
        DimensionAwareArgumentException("$dim dimension isn't exist in $ndim dimensions")
}