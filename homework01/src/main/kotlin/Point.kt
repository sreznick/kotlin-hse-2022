
interface Point: DimentionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(private vararg val points: Int) : Point {
    override val ndim: Int = points.size;
    override fun dim(i: Int): Int = points[i]

    override fun equals(other: Any?): Boolean {
        if (other is DefaultPoint) {
            return this.points.contentEquals(other.points)
        }
        return false
    }

    override fun hashCode(): Int {
        return points.hashCode()
    }
}
