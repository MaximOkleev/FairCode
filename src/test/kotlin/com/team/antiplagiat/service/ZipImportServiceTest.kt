package com.team.antiplagiat.service

import com.team.antiplagiat.config.props.ZipImportProperties
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.unit.DataSize
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipImportServiceTest {

    @Test
    fun `zip import properties have safe defaults`() {
        val props = ZipImportProperties()

        assertEquals(1000, props.maxFiles)
        assertEquals(DataSize.ofMegabytes(5), props.maxEntrySize)
    }

    @Test
    fun `importZip creates problem and solution for known user`() {
        var savedSolution: Solution? = null

        val problemRepository = repoProxy(ProblemRepository::class.java) { method, args ->
            when (method.name) {
                "findFirstByName" -> null
                "save" -> args?.firstOrNull()
                else -> defaultValue(method.returnType)
            }
        }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, args ->
            when (method.name) {
                "save" -> {
                    savedSolution = args?.firstOrNull() as Solution
                    savedSolution
                }
                else -> defaultValue(method.returnType)
            }
        }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ ->
            when (method.name) {
                "findByLogin" -> User(id = 1L, login = "ivan", email = "ivan@example.com")
                else -> defaultValue(method.returnType)
            }
        }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(multipartZip("Solutions/Task/ivan.cpp" to "int main() { return 0; }"))

        assertEquals(1, response.problemsCreated)
        assertEquals(1, response.solutionsCreated)
        assertEquals(0, response.skippedFiles)
        assertEquals(1, response.usersMatched)
        assertEquals(0, response.usersNotFound)
        assertTrue(response.errors.isEmpty())

        val saved = requireNotNull(savedSolution)
        assertEquals("ivan", saved.user.login)
        assertEquals("Task", saved.problem.name)
        assertEquals("Task/ivan.cpp", saved.filePath)
        assertEquals(SolutionStatus.WAITING, saved.status)
    }

    @Test
    fun `importZip skips file when user not found`() {
        val problemRepository = repoProxy(ProblemRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ ->
            when (method.name) {
                "findByLogin" -> null
                else -> defaultValue(method.returnType)
            }
        }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(multipartZip("Solutions/Task/unknown.cpp" to "int main() { return 0; }"))

        assertEquals(0, response.problemsCreated)
        assertEquals(0, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertEquals(0, response.usersMatched)
        assertEquals(1, response.usersNotFound)
        assertEquals(1, response.errors.size)
        assertTrue(response.errors[0].contains("Пользователь с логином 'unknown'"))
    }

    @Test
    fun `importZip supports both prefixed and direct task paths`() {
        val savedProblems = mutableListOf<Problem>()
        var callCount = 0

        val problemRepository = repoProxy(ProblemRepository::class.java) { method, args ->
            when (method.name) {
                "findFirstByName" -> {
                    callCount++
                    if (callCount == 1) null else savedProblems.first()
                }
                "save" -> {
                    val problem = args?.firstOrNull() as Problem
                    savedProblems += problem
                    problem
                }
                else -> defaultValue(method.returnType)
            }
        }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, args ->
            when (method.name) {
                "save" -> args?.firstOrNull()
                else -> defaultValue(method.returnType)
            }
        }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ ->
            when (method.name) {
                "findByLogin" -> when (callCount) {
                    0 -> User(id = 1L, login = "ivan", email = "ivan@example.com")
                    else -> User(id = 2L, login = "petr", email = "petr@example.com")
                }
                else -> defaultValue(method.returnType)
            }
        }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(
            multipartZip(
                "Solutions/Task/ivan.cpp" to "int main() { return 0; }",
                "Task/petr.java" to "class Main {}"
            )
        )

        assertEquals(1, response.problemsCreated)
        assertEquals(2, response.solutionsCreated)
        assertEquals(0, response.skippedFiles)
        assertEquals(2, response.usersMatched)
        assertEquals(0, response.usersNotFound)
    }

    @Test
    fun `importZip does not count matched user when solution save fails`() {
        val problemRepository = repoProxy(ProblemRepository::class.java) { method, _ ->
            when (method.name) {
                "findFirstByName" -> null
                "save" -> Problem(id = 1L, name = "Task", description = "Imported from ZIP")
                else -> defaultValue(method.returnType)
            }
        }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, _ ->
            when (method.name) {
                "save" -> throw RuntimeException("db failure")
                else -> defaultValue(method.returnType)
            }
        }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ ->
            when (method.name) {
                "findByLogin" -> User(id = 1L, login = "ivan", email = "ivan@example.com")
                else -> defaultValue(method.returnType)
            }
        }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(multipartZip("Solutions/Task/ivan.cpp" to "int main() { return 0; }"))

        assertEquals(1, response.problemsCreated)
        assertEquals(0, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertEquals(0, response.usersMatched)
        assertEquals(0, response.usersNotFound)
        assertTrue(response.errors.any { it.contains("Ошибка при обработке") })
    }

    @Test
    fun `importZip skips dangerous path`() {
        val problemRepository = repoProxy(ProblemRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ -> defaultValue(method.returnType) }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(multipartZip("../evil.cpp" to "int main() {}"))

        assertEquals(0, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertEquals(0, response.usersMatched)
        assertEquals(0, response.usersNotFound)
        assertTrue(response.errors.any { it.contains("Опасный или некорректный путь") })
    }

    @Test
    fun `importZip skips oversized file`() {
        val problemRepository = repoProxy(ProblemRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val props = ZipImportProperties().apply { maxEntrySize = DataSize.ofBytes(10) }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, props, SimpleMeterRegistry())
        val response = service.importZip(multipartZip("Solutions/Task/ivan.cpp" to "12345678901"))

        assertEquals(0, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertTrue(response.errors.any { it.contains("Файл слишком большой") })
    }

    @Test
    fun `importZip skips task with too long problem name`() {
        val problemRepository = repoProxy(ProblemRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ -> defaultValue(method.returnType) }

        val longProblemName = "a".repeat(201)
        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(multipartZip("$longProblemName/ivan.cpp" to "int main() { return 0; }"))

        assertEquals(0, response.problemsCreated)
        assertEquals(0, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertEquals(1, response.errors.size)
        assertTrue(response.errors.single().contains("Название задачи слишком длинное"))
    }

    @Test
    fun `importZip skips file when login extracted from file name is blank`() {
        val problemRepository = repoProxy(ProblemRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, _ -> defaultValue(method.returnType) }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ -> defaultValue(method.returnType) }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val response = service.importZip(multipartZip("Task/.cpp" to "int main() { return 0; }"))

        assertEquals(0, response.problemsCreated)
        assertEquals(0, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertEquals(1, response.errors.size)
        assertEquals("Не удалось извлечь login из имени файла: .cpp", response.errors.single())
    }

    @Test
    fun `importZip skips duplicate solution on repeated archive import`() {
        val savedProblems = mutableListOf<Problem>()
        val savedSolutions = mutableListOf<Solution>()
        val user = User(id = 1L, login = "ivan", email = "ivan@example.com")

        val problemRepository = repoProxy(ProblemRepository::class.java) { method, args ->
            when (method.name) {
                "findFirstByName" -> savedProblems.firstOrNull { it.name == args?.firstOrNull() as String }
                "save" -> {
                    val problem = args?.firstOrNull() as Problem
                    savedProblems += problem
                    problem
                }
                else -> defaultValue(method.returnType)
            }
        }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, args ->
            when (method.name) {
                "existsByUserAndProblemAndFilePath" -> {
                    val candidateUser = args?.getOrNull(0) as User
                    val candidateProblem = args.getOrNull(1) as Problem
                    val candidateFilePath = args.getOrNull(2) as String
                    savedSolutions.any {
                        it.user.id == candidateUser.id &&
                            it.problem.id == candidateProblem.id &&
                            it.filePath == candidateFilePath
                    }
                }
                "save" -> {
                    val solution = args?.firstOrNull() as Solution
                    savedSolutions += solution
                    solution
                }
                else -> defaultValue(method.returnType)
            }
        }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ ->
            when (method.name) {
                "findByLogin" -> user
                else -> defaultValue(method.returnType)
            }
        }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, ZipImportProperties(), SimpleMeterRegistry())
        val firstResponse = service.importZip(multipartZip("Solutions/Task/ivan.cpp" to "int main() { return 0; }"))
        val secondResponse = service.importZip(multipartZip("Solutions/Task/ivan.cpp" to "int main() { return 0; }"))

        assertEquals(1, firstResponse.solutionsCreated)
        assertEquals(0, firstResponse.skippedFiles)
        assertEquals(1, savedSolutions.size)

        assertEquals(0, secondResponse.solutionsCreated)
        assertEquals(1, secondResponse.skippedFiles)
        assertEquals(1, secondResponse.errors.size)
        assertEquals("Решение уже существует и было пропущено: Task/ivan.cpp", secondResponse.errors.single())
        assertEquals(1, savedSolutions.size)
    }

    @Test
    fun `importZip stops immediately after file limit exceeded`() {
        val savedProblems = mutableListOf<Problem>()
        val savedSolutions = mutableListOf<Solution>()

        val problemRepository = repoProxy(ProblemRepository::class.java) { method, args ->
            when (method.name) {
                "findFirstByName" -> savedProblems.firstOrNull { it.name == args?.firstOrNull() as String }
                "save" -> {
                    val problem = args?.firstOrNull() as Problem
                    savedProblems += problem
                    problem
                }
                else -> defaultValue(method.returnType)
            }
        }
        val solutionRepository = repoProxy(SolutionRepository::class.java) { method, args ->
            when (method.name) {
                "save" -> {
                    val solution = args?.firstOrNull() as Solution
                    savedSolutions += solution
                    solution
                }
                else -> defaultValue(method.returnType)
            }
        }
        val userRepository = repoProxy(UserRepository::class.java) { method, _ ->
            when (method.name) {
                "findByLogin" -> User(id = 1L, login = "ivan", email = "ivan@example.com")
                else -> defaultValue(method.returnType)
            }
        }
        val props = ZipImportProperties().apply { maxFiles = 1 }

        val service = ZipImportService(problemRepository, solutionRepository, userRepository, props, SimpleMeterRegistry())
        val response = service.importZip(
            multipartZip(
                "Solutions/Task/ivan.cpp" to "int main() { return 0; }",
                "Solutions/Task/petr.cpp" to "int main() { return 1; }",
                "Solutions/Task/sergey.cpp" to "int main() { return 2; }"
            )
        )

        assertEquals(1, response.solutionsCreated)
        assertEquals(1, response.skippedFiles)
        assertEquals(1, response.errors.size)
        assertEquals("Превышено максимальное количество файлов в архиве: 1", response.errors.single())
        assertEquals(1, savedSolutions.size)
        assertEquals(1, savedProblems.size)
    }

    private fun <T> repoProxy(type: Class<T>, handler: (Method, Array<out Any?>?) -> Any?): T {
        val proxy = Proxy.newProxyInstance(
            type.classLoader,
            arrayOf(type),
            object : InvocationHandler {
                override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
                    return when (method.name) {
                        "toString" -> "repoProxy(${type.simpleName})"
                        "hashCode" -> System.identityHashCode(this)
                        "equals" -> false
                        else -> handler(method, args)
                    }
                }
            }
        )
        @Suppress("UNCHECKED_CAST")
        return proxy as T
    }

    private fun defaultValue(type: Class<*>): Any? = when (type) {
        java.lang.Boolean.TYPE -> false
        java.lang.Byte.TYPE -> 0.toByte()
        java.lang.Short.TYPE -> 0.toShort()
        Int::class.javaPrimitiveType -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        Char::class.javaPrimitiveType -> '\u0000'
        else -> null
    }

    private fun multipartZip(vararg entries: Pair<String, String>): MockMultipartFile {
        val bytes = ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                entries.forEach { (path, content) ->
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(content.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
            }
            output.toByteArray()
        }

        return MockMultipartFile("file", "solutions.zip", "application/zip", bytes)
    }
}
