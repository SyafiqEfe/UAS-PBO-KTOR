package com.yourcompany.krs.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import com.yourcompany.krs.models.*

fun Application.dosenRoute() {
    routing {
        get("/dosen/{nidn}/mahasiswa") {
            val nidn = call.parameters["nidn"] ?: return@get call.respond(mapOf("success" to false, "message" to "NIDN wajib diisi"))
            val dosenId = transaction {
                DosenTable.select { DosenTable.nidn eq nidn }.singleOrNull()?.get(DosenTable.id)?.value
            }
            if (dosenId == null) {
                call.respond(mapOf("success" to false, "message" to "Dosen tidak ditemukan"))
                return@get
            }
            val mahasiswaList = transaction {
                MahasiswaTable.select { MahasiswaTable.dpaId eq dosenId }
                    .map { it[MahasiswaTable.nama] to it[MahasiswaTable.nim] }
            }
            call.respond(mapOf("success" to true, "mahasiswa" to mahasiswaList))
        }
    }
}
