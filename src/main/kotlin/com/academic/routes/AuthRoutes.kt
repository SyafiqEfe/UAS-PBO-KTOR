package com.academic.routes

import com.academic.dto.*
import com.academic.services.AuthService
import com.academic.models.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {

    post("/login") {
        try {
            val request = call.receive<LoginRequest>()

            when (request.role.uppercase()) {
                "MAHASISWA" -> {
                    val mahasiswa = authService.authenticateMahasiswa(request.identifier, request.password)
                    if (mahasiswa != null) {
                        call.respond(ApiResponse(true, "Login berhasil", mahasiswa))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "Login gagal", null))
                    }
                }
                "DOSEN" -> {
                    val dosen = authService.authenticateDosen(request.identifier, request.password)
                    if (dosen != null) {
                        call.respond(ApiResponse(true, "Login berhasil", dosen))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "Login gagal", null))
                    }
                }
                "ADMIN" -> {
                    val admin = authService.authenticateAdmin(request.identifier, request.password)
                    if (admin != null) {
                        call.respond(ApiResponse(true, "Login berhasil", admin))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "Login gagal", null))
                    }
                }
                else -> {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Role tidak valid", null))
                }
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Terjadi kesalahan: ${e.message}", null))
        }
    }
}
