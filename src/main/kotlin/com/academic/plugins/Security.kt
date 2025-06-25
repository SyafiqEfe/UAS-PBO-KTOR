package com.academic.plugins

import com.academic.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val authService = AuthService()
    
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                val token = tokenCredential.token
                val validation = authService.validateToken(token)
                
                if (validation != null) {
                    UserIdPrincipal(validation.first)
                } else {
                    null
                }
            }
        }
    }
}
