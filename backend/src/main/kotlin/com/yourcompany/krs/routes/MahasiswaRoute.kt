package com.yourcompany.krs.routes

import com.yourcompany.krs.models.GenericResponse
import com.yourcompany.krs.models.MahasiswaTable
import com.yourcompany.krs.models.RegisterSuccessResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SortOrder

fun Application.mahasiswaRoute() {
    routing {
        post("/register") {
            val params = call.receiveParameters()
            val nama = params["nama"] ?: return@post call.respond(GenericResponse(false, "Nama wajib diisi"))
            val password = params["password"] ?: return@post call.respond(GenericResponse(false, "Password wajib diisi"))

            // --- PERUBAHAN DI SINI: val diubah menjadi var ---
            var response: Any

            try {
                var generatedNim: String? = null
                transaction {
                    val lastId = MahasiswaTable.selectAll()
                        .orderBy(MahasiswaTable.id, SortOrder.DESC)
                        .limit(1)
                        .singleOrNull()?.get(MahasiswaTable.id)?.value ?: 0

                    val newId = lastId + 1
                    generatedNim = "NIM${newId.toString().padStart(5, '0')}"

                    MahasiswaTable.insert {
                        it[MahasiswaTable.nim] = generatedNim!!
                        it[MahasiswaTable.nama] = nama
                        it[MahasiswaTable.password] = password
                    }
                }
                response = RegisterSuccessResponse(success = true, nim = generatedNim!!)
            } catch (e: Exception) {
                response = GenericResponse(false, "Gagal mendaftarkan mahasiswa: ${e.message}")
            }

            call.respond(response)
        }
    }
}