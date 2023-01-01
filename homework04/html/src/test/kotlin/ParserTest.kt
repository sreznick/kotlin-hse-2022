import org.junit.jupiter.api.Test

internal class ParagraphTest{
    private val parserObj = ExtendedParsers().parseParagraph()
    @Test
    fun empty(){
        val text = ""
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<p></p>")
    }

    @Test
    fun dummyString(){
        val text = "dfsefsdf"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<p>$text</p>")
    }

    @Test
    fun onlyLeftTag(){
        val text = "<p>dfsefsdf"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "$text</p>")
    }

    @Test
    fun onlyRightTag(){
        val text = "dfsefsdf</p>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<p>$text")
    }

    @Test
    fun fullTags(){
        val text = "<p>dfsefsdf</p>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == text)
    }

    @Test
    fun error1(){
        val text = "<b>sdfsdfsre</b>"
        val res = parserObj(CharSource(text))
        assert(res is Left)
    }
    @Test
    fun error2(){
        val text = "sdf<b>sdfsre"
        val res = parserObj(CharSource(text))
        assert(res is Left)
    }
}


internal class DivTest{
    private val parserObj = ExtendedParsers().parseDiv()

    @Test
    fun empty(){
        val text = ""
        val res = parserObj(CharSource(text))
        assert(res is Left)
    }
    @Test
    fun onlyLeft(){
        val text = "<div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<div></div>")
    }

    @Test
    fun onlyRight(){
        val text = "</div>"
        val res = parserObj(CharSource(text))
        assert(res is Left)
    }

    @Test
    fun emptyDiv(){
        val text = "<div></div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<div></div>")
    }

    @Test
    fun divWithText(){
        val text = "<div>sdfgfse</div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<div><p>sdfgfse</p></div>")
    }

    @Test
    fun divNested1(){
        val text = "<div><div></div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<div><div></div></div>")
    }
    @Test
    fun divNested2(){
        val text = "<div><div></div></div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<div><div></div></div>")
    }

    @Test
    fun divNested3(){
        val text = "<div>3253<div><p>43</div>gdeg<div><div></div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<div><p>3253</p><div><p>43</p></div><p>gdeg</p><div><div></div></div></div>")
    }
}

internal class BodyTest{
    private val parserObj = ExtendedParsers().parseBody()
    @Test
    fun empty(){
        val text = ""
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<body></body>")
    }
    @Test
    fun onlyLeft(){
        val text = "<body>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<body></body>")
    }

    @Test
    fun onlyRight(){
        val text = "</body>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<body></body>")
    }

    @Test
    fun emptyBody(){
        val text = "<body></body>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<body></body>")
    }

    @Test
    fun onlyText(){
        val text = "sdfgfse"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<body><p>sdfgfse</p></body>")
    }

    @Test
    fun bigTest1(){
        val text = "<div>3253<div><p>43</div>gdeg<div><div></div>"
        val res = parserObj(CharSource(text))
        assert(res is Right)
        assert((res as Right).value == "<body><div><p>3253</p><div><p>43</p></div><p>gdeg</p><div><div></div></div></div></body>")
    }
}