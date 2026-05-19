package com.team.antiplagiat.exception

class InvalidCredentialsException(
    message: String = "Invalid email or password"
) : RuntimeException(message)