package homework04.parseUtils


interface CharParsers {
    fun char(c: Char): Parser<StringView, Char> = { input: StringView ->
        if (input.startsWith(c))
            Success(c, 1)
        else Failure(StringLocation(input).toError("Expected: $c"))
    }

    fun charSet(list: Set<Char>): Parser<StringView, Char> = { input: StringView ->
        if (input.isEmpty() or !list.contains(input.get(0)))
            Failure(StringLocation(input).toError("Expected one of: ${list.joinToString(", ")}"))
        else Success(input.get(0), 1)
    }

    fun charNotSet(list: Set<Char>): Parser<StringView, Char> = { input: StringView ->
        if (input.isEmpty() or list.contains(input.get(0)))
            Failure(StringLocation(input).toError("Expected not one of: ${list.joinToString(", ")}"))
        else Success(input.get(0), 1)
    }
}