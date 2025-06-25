package com.academic.plugins

import com.academic.routes.*
import com.academic.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val authService = AuthService()

    routing {
        authRoutes(authService)
        mahasiswaRoutes()
        dosenRoutes()
        adminRoutes()
    }
}
