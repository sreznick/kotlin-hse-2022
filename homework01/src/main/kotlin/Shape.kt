interface Shape : DimentionAware, SizeAware

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
class DefaultShape(private vararg val dimentions: Int) : Shape {
    init {
        if (dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        checkNotPositive();
    }

    private fun checkNotPositive() {
        if (dimentions.any { el -> el <= 0 }) {
            val res = dimentions.first { el -> el <= 0 }
            val resInd = dimentions.indexOf(res)
            throw ShapeArgumentException.NonPositiveDimensionException(resInd, res)
        }
    }

    override val ndim: Int
        get() = dimentions.size

    override fun dim(i: Int): Int = dimentions[i]

    override val size: Int
        get() = dimentions.reduce { res, current -> res * current }


}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException() : ShapeArgumentException("Empty shape found")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException(String.format("Found %d dimension at %d index", value, index))
}
