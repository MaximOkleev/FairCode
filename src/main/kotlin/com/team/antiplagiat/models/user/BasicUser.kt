package com.team.antiplagiat.models.user

import com.team.antiplagiat.models.Entity

interface BasicUser : Entity {
    var login: String
    var email: String
}