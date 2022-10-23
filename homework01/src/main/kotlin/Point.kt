
interface Point: DimentionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */

class DefaultPoint(vararg args: Int) : Point {
    private val coordinates = args.toList()
    override val ndim: Int = args.size
    override fun dim(i: Int): Int = coordinates[i]
}
