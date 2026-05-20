package com.team.antiplagiat.exception

class TokenExpiredException(
    message: String = "Token expired"
) : RuntimeException(message)

