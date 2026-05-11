package com.team.antiplagiat.service

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object TokenUtils {
    private val random = SecureRandom()
    private const val TOKEN_LENGTH = 32

    fun generateToken(): String {
        val bytes = ByteArray(TOKEN_LENGTH)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }
}

