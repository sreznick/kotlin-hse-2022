
interface Shape: DimensionAware, SizeAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultShape(10), DefaultShape(12, 3), DefaultShape(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * При попытке создать пустой Shape бросается EmptyShapeException
 *
 * При попытке указать неположительное число по любой размерности бросается NonPositiveDimensionException
 * Свойство index - минимальный индекс с некорректным значением, value - само значение
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultShape(private vararg val dimensions: Int): Shape {

    override val ndim: Int = dimensions.size

    override val size: Int
    init {
        if (dimensions.isEmpty()) throw ShapeArgumentException.EmptyShapeException("dimension can't be empty")
        var tsize = 1
        dimensions.forEachIndexed { idx, it ->
            if (it <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(
                    "non positive dimension in shape: dimension $idx with value $it", index = idx, value = it
                )
            }
            tsize *= it
        }
        this.size = tsize
    }
    override fun dim(i: Int): Int = dimensions[i]

    override fun equals(other: Any?): Boolean {
        if (other is DefaultShape) {
            if (this.ndim == other.ndim && this.size == other.size) {
                return this.dimensions.contentEquals(other.dimensions)
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return this.dimensions.hashCode()
    }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class NonPositiveDimensionException(reason : String = "",
                                        val index : Int,
                                        val value : Int) : ShapeArgumentException(reason)
    class EmptyShapeException(reason: String = "") : ShapeArgumentException(reason)
}