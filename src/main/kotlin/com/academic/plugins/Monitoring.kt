package com.academic.plugins

import io.ktor.server.application.*

fun Application.configureMonitoring() {
    // Temporarily disabled CallLogging to fix build issues
    // install(CallLogging) {
    //     level = Level.INFO
    //     filter { call -> call.request.path().startsWith("/") }
    // }
}
