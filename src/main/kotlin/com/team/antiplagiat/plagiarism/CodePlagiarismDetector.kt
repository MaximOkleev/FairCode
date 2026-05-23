package com.team.antiplagiat.plagiarism

data class CodePlagiarismResult(
    val isPlagiarism: Boolean,
    val similarity: Double,
    val threshold: Double,
    val matchedFragments: List<CodeMatchedFragment> = emptyList()
)

data class CodeMatchedFragment(
    val firstStartLine: Int,
    val firstEndLine: Int,
    val secondStartLine: Int,
    val secondEndLine: Int
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
        val similarity = calculateSimilarity(firstTokens.map { it.value }, secondTokens.map { it.value })

        return CodePlagiarismResult(
            isPlagiarism = similarity >= threshold,
            similarity = similarity,
            threshold = threshold,
            matchedFragments = findMatchedFragments(firstTokens, secondTokens)
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

    private fun findMatchedFragments(
        firstTokens: List<CodeToken>,
        secondTokens: List<CodeToken>
    ): List<CodeMatchedFragment> {
        if (firstTokens.isEmpty() || secondTokens.isEmpty()) {
            return emptyList()
        }

        val firstShingles = buildShinglesWithRanges(firstTokens)
        val secondShingles = buildShinglesWithRanges(secondTokens)

        val fragments = firstShingles.keys
            .intersect(secondShingles.keys)
            .flatMap { shingle ->
                firstShingles.getValue(shingle).flatMap { firstRange ->
                    secondShingles.getValue(shingle).map { secondRange ->
                        CodeMatchedFragment(
                            firstStartLine = firstRange.startLine,
                            firstEndLine = firstRange.endLine,
                            secondStartLine = secondRange.startLine,
                            secondEndLine = secondRange.endLine
                        )
                    }
                }
            }
            .distinct()

        return mergeMatchedFragments(fragments)
            .sortedWith(
                compareBy<CodeMatchedFragment> { it.firstStartLine }
                    .thenBy { it.secondStartLine }
                    .thenBy { it.firstEndLine }
                    .thenBy { it.secondEndLine }
            )
    }

    private fun mergeMatchedFragments(fragments: List<CodeMatchedFragment>): List<CodeMatchedFragment> {
        val merged = mutableListOf<CodeMatchedFragment>()

        fragments.forEach { fragment ->
            val mergeIndex = merged.indexOfFirst { it.canMergeWith(fragment) }
            if (mergeIndex == -1) {
                merged += fragment
            } else {
                merged[mergeIndex] = merged[mergeIndex].mergeWith(fragment)
            }
        }

        return if (merged.size == fragments.size) {
            merged
        } else {
            mergeMatchedFragments(merged)
        }
    }

    private fun CodeMatchedFragment.canMergeWith(other: CodeMatchedFragment): Boolean =
        firstStartLine <= other.firstEndLine + 1 &&
            other.firstStartLine <= firstEndLine + 1 &&
            secondStartLine <= other.secondEndLine + 1 &&
            other.secondStartLine <= secondEndLine + 1

    private fun CodeMatchedFragment.mergeWith(other: CodeMatchedFragment): CodeMatchedFragment =
        CodeMatchedFragment(
            firstStartLine = minOf(firstStartLine, other.firstStartLine),
            firstEndLine = maxOf(firstEndLine, other.firstEndLine),
            secondStartLine = minOf(secondStartLine, other.secondStartLine),
            secondEndLine = maxOf(secondEndLine, other.secondEndLine)
        )

    private fun buildShinglesWithRanges(tokens: List<CodeToken>): Map<String, List<LineRange>> {
        if (tokens.isEmpty()) {
            return emptyMap()
        }

        val windows = if (tokens.size <= SHINGLE_SIZE) {
            listOf(tokens)
        } else {
            tokens.windowed(SHINGLE_SIZE)
        }

        return windows
            .groupBy(
                keySelector = { window -> window.joinToString(TOKEN_SEPARATOR) { it.value } },
                valueTransform = { window ->
                    LineRange(
                        startLine = window.minOf { it.line },
                        endLine = window.maxOf { it.line }
                    )
                }
            )
    }

    private fun tokenize(code: String): List<CodeToken> {
        val tokens = mutableListOf<CodeToken>()
        var index = 0
        var line = 1

        while (index < code.length) {
            val char = code[index]

            index = when {
                char.isWhitespace() -> {
                    if (char == '\n') {
                        line++
                    }
                    index + 1
                }
                code.startsWith("//", index) -> {
                    val nextIndex = skipLineComment(code, index + 2)
                    line += countNewLines(code, index, nextIndex)
                    nextIndex
                }
                code.startsWith("/*", index) -> {
                    val nextIndex = skipBlockComment(code, index + 2)
                    line += countNewLines(code, index, nextIndex)
                    nextIndex
                }
                code.startsWith("\"\"\"", index) -> {
                    val nextIndex = skipTripleQuotedString(code, index + 3)
                    tokens += CodeToken(LITERAL_TOKEN, line)
                    line += countNewLines(code, index, nextIndex)
                    nextIndex
                }
                char == '"' || char == '\'' || char == '`' -> {
                    val nextIndex = skipQuotedLiteral(code, index + 1, char)
                    tokens += CodeToken(LITERAL_TOKEN, line)
                    line += countNewLines(code, index, nextIndex)
                    nextIndex
                }
                char.isDigit() -> {
                    tokens += CodeToken(LITERAL_TOKEN, line)
                    skipNumber(code, index + 1)
                }
                char == ';' -> index + 1
                char.isIdentifierStart() -> {
                    val nextIndex = skipIdentifier(code, index + 1)
                    val word = code.substring(index, nextIndex)
                    tokens += CodeToken(
                        value = if (word.lowercase() in keywords) word.lowercase() else IDENTIFIER_TOKEN,
                        line = line
                    )
                    nextIndex
                }
                char.isOperatorOrPunctuation() -> {
                    tokens += CodeToken(char.toString(), line)
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

    private fun countNewLines(code: String, startIndex: Int, endIndex: Int): Int =
        code.subSequence(startIndex, endIndex).count { it == '\n' }

    private data class CodeToken(
        val value: String,
        val line: Int
    )

    private data class LineRange(
        val startLine: Int,
        val endLine: Int
    )
}
