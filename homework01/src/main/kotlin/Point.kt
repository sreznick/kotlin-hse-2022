
interface Point: DimensionAware

class DefaultPoint (private vararg val coords: Int): Point  {
    override val ndim: Int = coords.size;
    override fun dim(i: Int): Int = coords[i]
}