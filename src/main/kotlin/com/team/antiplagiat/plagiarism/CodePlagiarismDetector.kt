package com.team.antiplagiat.plagiarism

data class CodePlagiarismResult(
    val isPlagiarism: Boolean,
    val similarity: Double,
    val threshold: Double
)

object CodePlagiarismDetector {
    const val DEFAULT_THRESHOLD = 0.8

    private const val SHINGLE_SIZE = 5
    private const val TOKEN_SEPARATOR = "\u0001"
    private const val IDENTIFIER_TOKEN = "IDENTIFIER"
    private const val LITERAL_TOKEN = "LITERAL"

    private val keywords = setOf(
        "abstract", "and", "as", "assert", "async", "await", "base", "bool", "boolean",
        "break", "by", "byte", "case", "catch", "char", "class", "const", "constructor",
        "continue", "data", "def", "default", "delegate", "do", "double", "dynamic",
        "elif", "else", "enum", "except", "export", "extends", "false", "file", "final",
        "finally", "float", "for", "from", "fun", "function", "get", "global", "goto",
        "if", "implements", "import", "in", "init", "inline", "int", "interface",
        "internal", "is", "lambda", "lateinit", "let", "long", "namespace", "new",
        "none", "not", "null", "object", "open", "operator", "or", "out", "override",
        "package", "params", "private", "protected", "public", "raise", "return",
        "sealed", "set", "short", "static", "struct", "super", "switch", "suspend",
        "template", "this", "throw", "throws", "trait", "true", "try", "typealias",
        "typeof", "using", "val", "var", "virtual", "void", "when", "where", "while",
        "with", "yield"
    )

    fun check(
        firstCode: String,
        secondCode: String,
        threshold: Double = DEFAULT_THRESHOLD
    ): CodePlagiarismResult {
        require(threshold in 0.0..1.0) { "threshold must be in range 0.0..1.0" }

        val firstTokens = tokenize(firstCode)
        val secondTokens = tokenize(secondCode)
        val similarity = calculateSimilarity(firstTokens, secondTokens)

        return CodePlagiarismResult(
            isPlagiarism = similarity >= threshold,
            similarity = similarity,
            threshold = threshold
        )
    }

    fun isPlagiarism(
        firstCode: String,
        secondCode: String,
        threshold: Double = DEFAULT_THRESHOLD
    ): Boolean = check(firstCode, secondCode, threshold).isPlagiarism

    private fun calculateSimilarity(firstTokens: List<String>, secondTokens: List<String>): Double {
        if (firstTokens.isEmpty() && secondTokens.isEmpty()) {
            return 0.0
        }

        if (firstTokens == secondTokens) {
            return 1.0
        }

        if (firstTokens.isEmpty() || secondTokens.isEmpty()) {
            return 0.0
        }

        val firstShingles = buildShingles(firstTokens)
        val secondShingles = buildShingles(secondTokens)
        val intersectionSize = firstShingles.intersect(secondShingles).size
        val unionSize = firstShingles.union(secondShingles).size

        return if (unionSize == 0) 0.0 else intersectionSize.toDouble() / unionSize
    }

    private fun buildShingles(tokens: List<String>): Set<String> {
        if (tokens.isEmpty()) {
            return emptySet()
        }

        if (tokens.size <= SHINGLE_SIZE) {
            return setOf(tokens.joinToString(TOKEN_SEPARATOR))
        }

        return tokens
            .windowed(SHINGLE_SIZE)
            .map { it.joinToString(TOKEN_SEPARATOR) }
            .toSet()
    }

    private fun tokenize(code: String): List<String> {
        val tokens = mutableListOf<String>()
        var index = 0

        while (index < code.length) {
            val char = code[index]

            index = when {
                char.isWhitespace() -> index + 1
                code.startsWith("//", index) -> skipLineComment(code, index + 2)
                code.startsWith("/*", index) -> skipBlockComment(code, index + 2)
                code.startsWith("\"\"\"", index) -> {
                    tokens += LITERAL_TOKEN
                    skipTripleQuotedString(code, index + 3)
                }
                char == '"' || char == '\'' || char == '`' -> {
                    tokens += LITERAL_TOKEN
                    skipQuotedLiteral(code, index + 1, char)
                }
                char.isDigit() -> {
                    tokens += LITERAL_TOKEN
                    skipNumber(code, index + 1)
                }
                char == ';' -> index + 1
                char.isIdentifierStart() -> {
                    val nextIndex = skipIdentifier(code, index + 1)
                    val word = code.substring(index, nextIndex)
                    tokens += if (word.lowercase() in keywords) word.lowercase() else IDENTIFIER_TOKEN
                    nextIndex
                }
                char.isOperatorOrPunctuation() -> {
                    tokens += char.toString()
                    index + 1
                }
                else -> index + 1
            }
        }

        return tokens
    }

    private fun skipLineComment(code: String, startIndex: Int): Int {
        var index = startIndex
        while (index < code.length && code[index] != '\n') {
            index++
        }
        return index
    }

    private fun skipBlockComment(code: String, startIndex: Int): Int {
        var index = startIndex
        while (index + 1 < code.length && !code.startsWith("*/", index)) {
            index++
        }
        return (index + 2).coerceAtMost(code.length)
    }

    private fun skipTripleQuotedString(code: String, startIndex: Int): Int {
        var index = startIndex
        while (index + 2 < code.length && !code.startsWith("\"\"\"", index)) {
            index++
        }
        return (index + 3).coerceAtMost(code.length)
    }

    private fun skipQuotedLiteral(code: String, startIndex: Int, quote: Char): Int {
        var index = startIndex
        var escaped = false

        while (index < code.length) {
            val current = code[index]

            if (current == quote && !escaped) {
                return index + 1
            }

            escaped = current == '\\' && !escaped
            if (current != '\\') {
                escaped = false
            }
            index++
        }

        return code.length
    }

    private fun skipNumber(code: String, startIndex: Int): Int {
        var index = startIndex
        while (
            index < code.length &&
            (code[index].isLetterOrDigit() || code[index] == '_' || code[index] == '.')
        ) {
            index++
        }
        return index
    }

    private fun skipIdentifier(code: String, startIndex: Int): Int {
        var index = startIndex
        while (index < code.length && code[index].isIdentifierPart()) {
            index++
        }
        return index
    }

    private fun Char.isIdentifierStart(): Boolean = this == '_' || this == '$' || isLetter()

    private fun Char.isIdentifierPart(): Boolean = isIdentifierStart() || isDigit()

    private fun Char.isOperatorOrPunctuation(): Boolean =
        this in "{}[]().,;:+-*/%=&|!<>?^~@#"
}
