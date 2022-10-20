
interface DimensionAware {
    val dimNumber: Int

    /*
    * 0 <= i < dimNumber
    * result is non-negative
    * */
    fun dim(i: Int): Int

    fun extendedDimensions(other: DimensionAware, suffix: Int): Boolean {
        return dimNumber + suffix == other.dimNumber && (0 until dimNumber).all { dim(it) == other.dim(it) }
    }

    fun equalDimensions(other: DimensionAware): Boolean = extendedDimensions(other, 0)

    fun cutPrefix(prefixLen: Int): DimensionAware {
        return object : DimensionAware {
            override val dimNumber = this@DimensionAware.dimNumber - prefixLen

            override fun dim(i: Int): Int = this@DimensionAware.dim(i) // UB if i > dimNumber
        }
    }
}

interface SizeAware {
    val size: Int
}