package com.team.antiplagiat.exception

class TokenAlreadyUsedException(
    message: String = "Token already used"
) : RuntimeException(message)

