
interface Point: DimentionAware

class DefaultPoint(private vararg val coordinates: Int): Point {

    override val ndim: Int = coordinates.size
    override fun dim(i: Int): Int = coordinates[i]

}