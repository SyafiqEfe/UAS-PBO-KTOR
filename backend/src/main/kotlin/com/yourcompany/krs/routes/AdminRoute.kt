package com.yourcompany.krs.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import com.yourcompany.krs.models.* // Pastikan semua import models ada
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select

fun Application.adminRoute() {
    routing {
        // GET all Mahasiswa
        get("/admin/mahasiswa") {
            val mahasiswaList = transaction {
                MahasiswaTable.selectAll().map {
                    MahasiswaResponse( // <-- Gunakan data class MahasiswaResponse
                        id = it[MahasiswaTable.id].value,
                        nim = it[MahasiswaTable.nim],
                        nama = it[MahasiswaTable.nama],
                        dpaId = it[MahasiswaTable.dpaId]
                    )
                }
            }
            // <-- Gunakan data class MahasiswaListResponse
            call.respond(MahasiswaListResponse(success = true, mahasiswa = mahasiswaList))
        }

        // POST add Mahasiswa (tetap sama, karena sudah GenericResponse)
        post("/admin/mahasiswa") {
            val params = call.receiveParameters()
            val nim = params["nim"] ?: return@post call.respond(GenericResponse(false, "NIM wajib diisi"))
            val nama = params["nama"] ?: return@post call.respond(GenericResponse(false, "Nama wajib diisi"))
            val password = params["password"] ?: return@post call.respond(GenericResponse(false, "Password wajib diisi"))
            val dpaId = params["dpaId"]?.toIntOrNull()

            try {
                val insertCount = transaction {
                    MahasiswaTable.insert {
                        it[MahasiswaTable.nim] = nim
                        it[MahasiswaTable.nama] = nama
                        it[MahasiswaTable.password] = password
                        it[MahasiswaTable.dpaId] = dpaId
                    }.insertedCount
                }
                if (insertCount > 0) {
                    call.respond(GenericResponse(true, "Mahasiswa berhasil ditambahkan"))
                } else {
                    call.respond(GenericResponse(false, "Gagal menambahkan mahasiswa"))
                }
            } catch (e: Exception) {
                call.respond(GenericResponse(false, "Error: ${e.message}"))
            }
        }

        // DELETE Mahasiswa (tetap sama, karena sudah GenericResponse)
        delete("/admin/mahasiswa/{nim}") {
            val nim = call.parameters["nim"] ?: return@delete call.respond(GenericResponse(false, "NIM wajib diisi"))
            val deletedCount = transaction {
                MahasiswaTable.deleteWhere { MahasiswaTable.nim eq nim }
            }
            if (deletedCount > 0) {
                call.respond(GenericResponse(true, "Mahasiswa $nim berhasil dihapus"))
            } else {
                call.respond(GenericResponse(false, "Mahasiswa $nim tidak ditemukan"))
            }
        }

        // GET all Dosen
        get("/admin/dosen") {
            val dosenList = transaction {
                DosenTable.selectAll().map {
                    DosenResponse( // <-- Gunakan data class DosenResponse
                        id = it[DosenTable.id].value, // Pastikan ID ini diambil
                        nidn = it[DosenTable.nidn],
                        nama = it[DosenTable.nama]
                    )
                }
            }
            // <-- Gunakan data class DosenListResponse
            call.respond(DosenListResponse(success = true, dosen = dosenList))
        }

        // POST add Dosen (tetap sama, karena sudah GenericResponse)
        post("/admin/dosen") {
            val params = call.receiveParameters()
            val nidn = params["nidn"] ?: return@post call.respond(GenericResponse(false, "NIDN wajib diisi"))
            val nama = params["nama"] ?: return@post call.respond(GenericResponse(false, "Nama wajib diisi"))
            val password = params["password"] ?: return@post call.respond(GenericResponse(false, "Password wajib diisi"))

            try {
                val insertCount = transaction {
                    DosenTable.insert {
                        it[DosenTable.nidn] = nidn
                        it[DosenTable.nama] = nama
                        it[DosenTable.password] = password
                    }.insertedCount
                }
                if (insertCount > 0) {
                    call.respond(GenericResponse(true, "Dosen berhasil ditambahkan"))
                } else {
                    call.respond(GenericResponse(false, "Gagal menambahkan dosen"))
                }
            } catch (e: Exception) {
                call.respond(GenericResponse(false, "Error: ${e.message}"))
            }
        }

        // DELETE Dosen (tetap sama, karena sudah GenericResponse)
        delete("/admin/dosen/{nidn}") {
            val nidn = call.parameters["nidn"] ?: return@delete call.respond(GenericResponse(false, "NIDN wajib diisi"))
            val deletedCount = transaction {
                DosenTable.deleteWhere { DosenTable.nidn eq nidn }
            }
            if (deletedCount > 0) {
                call.respond(GenericResponse(true, "Dosen $nidn berhasil dihapus"))
            } else {
                call.respond(GenericResponse(false, "Dosen $nidn tidak ditemukan"))
            }
        }
        
        // GET all Mata Kuliah
        get("/admin/matakuliah") {
            val matkulList = transaction {
                MataKuliahTable.selectAll().map {
                    val dosenId = it[MataKuliahTable.dosenId].value
                    val dosenNama = DosenTable.slice(DosenTable.nama)
                                            .select { DosenTable.id eq dosenId }
                                            .singleOrNull()
                                            ?.get(DosenTable.nama) ?: "-"
                    MatakuliahAdminResponse( // <-- Gunakan data class MatakuliahAdminResponse
                        id = it[MataKuliahTable.id].value,
                        kode = it[MataKuliahTable.kode],
                        nama = it[MataKuliahTable.nama],
                        sks = it[MataKuliahTable.sks],
                        dosenId = dosenId,
                        dosen = dosenNama,
                        ruangan = it[MataKuliahTable.ruangan],
                        jamMulai = it[MataKuliahTable.jamMulai]
                    )
                }
            }
            // <-- Gunakan data class MatakuliahListAdminResponse
            call.respond(MatakuliahListAdminResponse(success = true, matakuliah = matkulList))
        }

        // POST add Mata Kuliah (tetap sama, karena sudah GenericResponse)
        post("/admin/matakuliah") {
            val params = call.receiveParameters()
            val kode = params["kode"] ?: return@post call.respond(GenericResponse(false, "Kode mata kuliah wajib diisi"))
            val nama = params["nama"] ?: return@post call.respond(GenericResponse(false, "Nama mata kuliah wajib diisi"))
            val sks = params["sks"]?.toIntOrNull() ?: return@post call.respond(GenericResponse(false, "SKS wajib diisi dan harus angka"))
            val dosenId = params["dosenId"]?.toIntOrNull() ?: return@post call.respond(GenericResponse(false, "Dosen ID wajib diisi dan harus angka"))
            val ruangan = params["ruangan"] ?: return@post call.respond(GenericResponse(false, "Ruangan wajib diisi"))
            val jamMulai = params["jamMulai"] ?: return@post call.respond(GenericResponse(false, "Jam Mulai wajib diisi"))

            try {
                val insertCount = transaction {
                    MataKuliahTable.insert {
                        it[MataKuliahTable.kode] = kode
                        it[MataKuliahTable.nama] = nama
                        it[MataKuliahTable.sks] = sks
                        it[MataKuliahTable.dosenId] = dosenId
                        it[MataKuliahTable.ruangan] = ruangan
                        it[MataKuliahTable.jamMulai] = jamMulai
                    }.insertedCount
                }
                if (insertCount > 0) {
                    call.respond(GenericResponse(true, "Mata Kuliah berhasil ditambahkan"))
                } else {
                    call.respond(GenericResponse(false, "Gagal menambahkan mata kuliah"))
                }
            } catch (e: Exception) {
                call.respond(GenericResponse(false, "Error: ${e.message}"))
            }
        }

        // DELETE Mata Kuliah (tetap sama, karena sudah GenericResponse)
        delete("/admin/matakuliah/{kode}") {
            val kode = call.parameters["kode"] ?: return@delete call.respond(GenericResponse(false, "Kode mata kuliah wajib diisi"))
            val deletedCount = transaction {
                MataKuliahTable.deleteWhere { MataKuliahTable.kode eq kode }
            }
            if (deletedCount > 0) {
                call.respond(GenericResponse(true, "Mata Kuliah $kode berhasil dihapus"))
            } else {
                call.respond(GenericResponse(false, "Mata Kuliah $kode tidak ditemukan"))
            }
        }
    }
}