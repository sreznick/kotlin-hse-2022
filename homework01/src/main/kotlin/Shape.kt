
interface Shape: DimentionAware, SizeAware

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
class DefaultShape(private vararg val dimentions: Int): Shape {

    init {
        if (dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException();
        }
        for (idx in dimentions.indices) {
            if (dimentions[idx] < 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(idx, dimentions[idx]);
            }
        }
    }

    override val ndim: Int
        get() = dimentions.size

    override fun dim(i: Int): Int {
        return dimentions[i]
    }

    override val size: Int
        get() = dimentions.reduce { res, it -> res * it }

}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException: ShapeArgumentException("Empty shape exception")
    class NonPositiveDimensionException(private val index: Int, private val value: Int) :
        ShapeArgumentException("Non-positive value: $value at index $index")
}
