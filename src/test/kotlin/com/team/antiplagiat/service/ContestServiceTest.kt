package com.team.antiplagiat.service

import com.team.antiplagiat.config.ContestConfig
import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.ContestRepository
import com.team.antiplagiat.repository.UserRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockKExtension::class)
class ContestServiceTest {

    @MockK
    private lateinit var contestRepository: ContestRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var config: ContestConfig

    @InjectMockKs
    private lateinit var contestService: ContestService

    @Test
    fun `create returns contest when admin exists and duration is within limit`() {
        val admin = User(id = 1L, login = "admin", email = "admin@example.com", role = User.Role.ADMIN)
        val contest = Contest(
            name = "Spring Contest",
            admin = User(id = 1L),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600
        )

        every { config.maxDurationHours } returns 2
        every { userRepository.findById(1L) } returns Optional.of(admin)
        every { contestRepository.save(any()) } answers { firstArg() }

        val result = contestService.create(contest)

        assertEquals("Spring Contest", result?.name)
        assertEquals(3600, result?.duration)
        assertEquals(admin.id, result?.admin?.id)
        verify(exactly = 1) { userRepository.findById(1L) }
        verify(exactly = 1) { contestRepository.save(contest) }
    }

    @Test
    fun `create returns null when duration exceeds max limit`() {
        val contest = Contest(
            name = "Too Long",
            admin = User(id = 1L),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 7201
        )

        every { config.maxDurationHours } returns 2

        val result = contestService.create(contest)

        assertNull(result)
        verify(exactly = 0) { userRepository.findById(any()) }
        verify(exactly = 0) { contestRepository.save(any()) }
    }

    @Test
    fun `create returns null when admin does not exist`() {
        val contest = Contest(
            name = "Missing Admin",
            admin = User(id = 99L),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600
        )

        every { config.maxDurationHours } returns 2
        every { userRepository.findById(99L) } returns Optional.empty()

        val result = contestService.create(contest)

        assertNull(result)
        verify(exactly = 1) { userRepository.findById(99L) }
        verify(exactly = 0) { contestRepository.save(any()) }
    }

    @Test
    fun `create returns null when admin is not an administrator`() {
        val user = User(id = 2L, login = "basic", email = "basic@example.com", role = User.Role.BASIC)
        val contest = Contest(
            name = "Wrong Role",
            admin = User(id = 2L),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600
        )

        every { config.maxDurationHours } returns 2
        every { userRepository.findById(2L) } returns Optional.of(user)

        val result = contestService.create(contest)

        assertNull(result)
        verify(exactly = 1) { userRepository.findById(2L) }
        verify(exactly = 0) { contestRepository.save(any()) }
    }

    @Test
    fun `findById returns contest when repository has value`() {
        val contest = Contest(id = 10L, name = "Lookup", admin = User(id = 1L))
        every { contestRepository.findById(10L) } returns Optional.of(contest)

        val result = contestService.findById(10L)

        assertEquals(10L, result?.id)
        assertEquals("Lookup", result?.name)
    }

    @Test
    fun `findById returns null when contest is missing`() {
        every { contestRepository.findById(10L) } returns Optional.empty()

        val result = contestService.findById(10L)

        assertNull(result)
    }

    @Test
    fun `findAll returns all contests`() {
        val contests = listOf(
            Contest(id = 1L, name = "Contest 1", admin = User(id = 1L)),
            Contest(id = 2L, name = "Contest 2", admin = User(id = 1L))
        )
        every { contestRepository.findAll() } returns contests

        val result = contestService.findAll()

        assertEquals(2, result.size)
        assertEquals("Contest 1", result[0].name)
        assertEquals("Contest 2", result[1].name)
    }

    @Test
    fun `findByAdmin returns contests for specific admin`() {
        val contests = listOf(
            Contest(id = 1L, name = "Contest 1", admin = User(id = 5L)),
            Contest(id = 2L, name = "Contest 2", admin = User(id = 5L))
        )
        every { contestRepository.findAllByAdminId(5L) } returns contests

        val result = contestService.findByAdmin(5L)

        assertEquals(2, result.size)
        assertEquals("Contest 1", result[0].name)
        assertEquals("Contest 2", result[1].name)
    }

    @Test
    fun `update changes name and duration when contest exists`() {
        val contest = Contest(
            id = 1L,
            name = "Old name",
            admin = User(id = 1L),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600
        )

        every { config.maxDurationHours } returns 2
        every { contestRepository.findById(1L) } returns Optional.of(contest)
        every { contestRepository.save(contest) } answers { firstArg() }

        val result = contestService.update(1L, "New name", 5400)

        assertEquals("New name", result?.name)
        assertEquals(5400, result?.duration)
        verify(exactly = 1) { contestRepository.save(contest) }
    }

    @Test
    fun `update returns null when contest does not exist`() {
        every { contestRepository.findById(1L) } returns Optional.empty()

        val result = contestService.update(1L, "New name", 5400)

        assertNull(result)
        verify(exactly = 0) { contestRepository.save(any()) }
    }

    @Test
    fun `update returns null when new duration exceeds limit`() {
        val contest = Contest(
            id = 1L,
            name = "Old name",
            admin = User(id = 1L),
            startedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
            duration = 3600
        )

        every { config.maxDurationHours } returns 2
        every { contestRepository.findById(1L) } returns Optional.of(contest)

        val result = contestService.update(1L, null, 7201)

        assertNull(result)
        verify(exactly = 0) { contestRepository.save(any()) }
    }

    @Test
    fun `delete calls repository deleteById`() {
        every { contestRepository.deleteById(1L) } just Runs

        contestService.delete(1L)

        verify(exactly = 1) { contestRepository.deleteById(1L) }
    }
}

