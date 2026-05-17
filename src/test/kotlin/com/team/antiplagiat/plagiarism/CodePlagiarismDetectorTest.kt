package com.team.antiplagiat.plagiarism

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CodePlagiarismDetectorTest {

    @Test
    fun `check should mark identical code as plagiarism`() {
        val code = """
            fun sum(a: Int, b: Int): Int {
                return a + b
            }
        """.trimIndent()

        val result = CodePlagiarismDetector.check(code, code)

        assertTrue(result.isPlagiarism)
        assertEquals(1.0, result.similarity)
    }

    @Test
    fun `check should ignore formatting comments and renamed identifiers`() {
        val firstCode = """
            fun maxValue(first: Int, second: Int): Int {
                // choose greater value
                if (first > second) {
                    return first
                }
                return second
            }
        """.trimIndent()
        val secondCode = """
            fun biggest(x:Int,y:Int):Int{if(x>y){return x}return y}
        """.trimIndent()

        val result = CodePlagiarismDetector.check(firstCode, secondCode)

        assertTrue(result.isPlagiarism)
        assertTrue(result.similarity >= CodePlagiarismDetector.DEFAULT_THRESHOLD)
    }

    @Test
    fun `check should ignore repeated whitespace and redundant semicolons`() {
        val firstCode = """
            fun sum(a: Int, b: Int): Int {
                return a + b
            }
        """.trimIndent()
        val secondCode = """
            fun    add  ( x : Int ,   y : Int ) : Int {;;;
                return   x   +   y;;;;;
            ;}
        """.trimIndent()

        val result = CodePlagiarismDetector.check(firstCode, secondCode)

        assertTrue(result.isPlagiarism)
        assertEquals(1.0, result.similarity)
    }

    @Test
    fun `check should mark substantially different code as not plagiarism`() {
        val firstCode = """
            fun factorial(n: Int): Int {
                var result = 1
                for (i in 2..n) {
                    result *= i
                }
                return result
            }
        """.trimIndent()
        val secondCode = """
            fun isPrime(number: Int): Boolean {
                if (number < 2) return false
                for (divider in 2 until number) {
                    if (number % divider == 0) return false
                }
                return true
            }
        """.trimIndent()

        val result = CodePlagiarismDetector.check(firstCode, secondCode)

        assertFalse(result.isPlagiarism)
        assertTrue(result.similarity < CodePlagiarismDetector.DEFAULT_THRESHOLD)
    }

    @Test
    fun `isPlagiarism should use custom threshold`() {
        val firstCode = "fun sum(a: Int, b: Int) = a + b"
        val secondCode = "fun add(x: Int, y: Int) = x + y"

        assertTrue(CodePlagiarismDetector.isPlagiarism(firstCode, secondCode, threshold = 0.6))
    }

    @Test
    fun `check should not mark empty snippets as plagiarism`() {
        val result = CodePlagiarismDetector.check("", "")

        assertFalse(result.isPlagiarism)
        assertEquals(0.0, result.similarity)
    }

    @Test
    fun `check should reject threshold outside expected range`() {
        assertThrows(IllegalArgumentException::class.java) {
            CodePlagiarismDetector.check("code", "code", threshold = 1.1)
        }
    }
}
