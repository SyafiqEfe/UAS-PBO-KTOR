package com.yourcompany.krs.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.healthRoute() {
    routing {
        get("/health") {
            call.respondText("OK")
        }
    }
}
