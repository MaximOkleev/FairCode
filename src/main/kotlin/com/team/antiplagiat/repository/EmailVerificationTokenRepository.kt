package com.team.antiplagiat.repository

import com.team.antiplagiat.models.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByTokenHash(tokenHash: String): EmailVerificationToken?

    fun findFirstByUserIdAndUsedAtIsNullOrderByIdDesc(userId: Long): EmailVerificationToken?

    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.userId = :userId AND e.usedAt IS NULL")
    fun deleteActiveByUserId(@Param("userId") userId: Long)
}

