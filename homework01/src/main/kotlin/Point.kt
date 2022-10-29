
interface Point: DimentionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(private vararg val coordinates: Int): Point {
    override val ndim: Int = coordinates.size
    override fun dim(i: Int): Int = coordinates[i]

    override fun toString(): String = buildString {
        append("Point:")
        coordinates.map { append(" $it") }
    }
}