interface Shape: DimensionAware, SizeAware

class DefaultShape(private vararg val dimensions: Int) : Shape {
    init {
        if (dimensions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        for (i in dimensions.indices) {
            if (dimensions[i] <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(i, dimensions[i])
            }
        }
    }

    override val ndim: Int = dimensions.size;
    override fun dim(i: Int): Int = dimensions[i]
    override val size: Int = dimensions.reduce{ accumulator, element -> accumulator * element }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Cannot create empty shape")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("Non-Positive dimension '$value' at position: $index")
}
