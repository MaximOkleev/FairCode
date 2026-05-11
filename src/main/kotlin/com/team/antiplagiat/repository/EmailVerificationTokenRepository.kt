package com.team.antiplagiat.repository

import com.team.antiplagiat.models.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByTokenHash(tokenHash: String): EmailVerificationToken?

    @Query("SELECT e FROM EmailVerificationToken e WHERE e.userId = :userId AND e.usedAt IS NULL ORDER BY e.id DESC LIMIT 1")
    fun findLatestByUserId(@Param("userId") userId: Long): EmailVerificationToken?

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken e WHERE e.userId = :userId AND e.usedAt IS NULL")
    fun deleteActiveByUserId(@Param("userId") userId: Long)
}

