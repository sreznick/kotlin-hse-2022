
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
    val coordinates = ArrayList<Int>()
    private val nsize : Int
    init {
        var forSize = 1
        var index = 0
        for (par : Int in dimentions) {
            if(par <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(index, par)
            }
            forSize *= par
            coordinates.add(par)
            index++
        }
        if (index == 0) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        nsize = forSize
    }
    override val ndim: Int
        get() = coordinates.size

    override fun dim(i: Int): Int {
        return coordinates[i]
    }

    override val size: Int
        get() = nsize
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException() : ShapeArgumentException("Empty Shape")
    class NonPositiveDimensionException(val index: Int, val value: Int) : ShapeArgumentException("Got $value at $index instead of Positive value")
}
