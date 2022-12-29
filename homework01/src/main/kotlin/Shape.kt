
interface Shape: DimentionAware, SizeAware

class DefaultShape(private vararg val dimentions: Int): Shape {
    init {
        if (dimentions.isEmpty()) throw ShapeArgumentException.EmptyShapeException();
    }

    override val ndim = dimentions.size
    override val size = dimentions.foldIndexed(1) {
       ind, res, i -> if (i <= 0) throw ShapeArgumentException.NonPositiveDimensionException(ind, i) else res * i
    }
    override fun dim(i : Int) = dimentions[i]
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Trying to create empty shape");

    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("Trying to create shape with non positive dimension $value at position $index.")
}
