package com.team.antiplagiat.exception

class RateLimitException(
    message: String = "Too many requests. Try again later"
) : RuntimeException(message)

