package com.yourcompany.krs.routes

import com.yourcompany.krs.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.id.EntityID

fun Application.krsRoute() {
    routing {
        // Endpoint untuk submit atau update KRS
        post("/krs/submit") {
            val params = call.receiveParameters()
            val nim = params["nim"] ?: return@post call.respond(GenericResponse(false, "NIM wajib diisi"))
            val matkulIds = params.getAll("matkulIds[]")?.mapNotNull { it.toIntOrNull() } ?: emptyList()

            val mahasiswaId = transaction {
                MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()?.get(MahasiswaTable.id)?.value
            }
            if (mahasiswaId == null) {
                return@post call.respond(GenericResponse(false, "Mahasiswa tidak ditemukan"))
            }

            val totalSks = transaction {
                MataKuliahTable.select { MataKuliahTable.id inList matkulIds }.sumOf { it[MataKuliahTable.sks] }
            }
            if (totalSks !in 19..24) {
                return@post call.respond(GenericResponse(false, "Total SKS harus antara 19-24"))
            }

            transaction {
                val existingKrs = KRSTable.select { KRSTable.mahasiswaId eq mahasiswaId }.singleOrNull()
                if (existingKrs != null) {
                    val krsId = existingKrs[KRSTable.id]
                    KRSDetailTable.deleteWhere { KRSDetailTable.krsId eq krsId }
                    matkulIds.forEach { mid ->
                        KRSDetailTable.insert {
                            it[KRSDetailTable.krsId] = krsId
                            it[KRSDetailTable.matkulId] = EntityID(mid, MataKuliahTable)
                        }
                    }
                } else {
                    val newKrsId = KRSTable.insertAndGetId {
                        it[KRSTable.mahasiswaId] = EntityID(mahasiswaId, MahasiswaTable)
                        it[KRSTable.status] = "Diajukan"
                    }
                    matkulIds.forEach { mid ->
                        KRSDetailTable.insert {
                            it[KRSDetailTable.krsId] = newKrsId
                            it[KRSDetailTable.matkulId] = EntityID(mid, MataKuliahTable)
                        }
                    }
                }
            }
            call.respond(GenericResponse(true, "KRS berhasil disimpan!"))
        }

        // Endpoint untuk mendapatkan data KRS seorang mahasiswa
        get("/krs/{nim}") {
            val nim = call.parameters["nim"] ?: return@get call.respond(GenericResponse(false, "NIM wajib diisi"))
            val mahasiswaId = transaction {
                MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()?.get(MahasiswaTable.id)?.value
            }
            if (mahasiswaId == null) {
                return@get call.respond(GenericResponse(false, "Mahasiswa tidak ditemukan"))
            }

            val krsList = transaction {
                KRSTable.select { KRSTable.mahasiswaId eq mahasiswaId }.map { krsRow ->
                    val krsId = krsRow[KRSTable.id].value
                    val matkul = KRSDetailTable
                        .innerJoin(MataKuliahTable, { matkulId }, { id })
                        .innerJoin(DosenTable, { MataKuliahTable.dosenId }, { id })
                        .select { KRSDetailTable.krsId eq krsRow[KRSTable.id] }
                        .map { detailRow ->
                            MatakuliahResponse(
                                id = detailRow[MataKuliahTable.id].value,
                                kode = detailRow[MataKuliahTable.kode],
                                nama = detailRow[MataKuliahTable.nama],
                                sks = detailRow[MataKuliahTable.sks],
                                dosen = detailRow[DosenTable.nama],
                                ruangan = detailRow[MataKuliahTable.ruangan],
                                jamMulai = detailRow[MataKuliahTable.jamMulai]
                            )
                        }
                    KRSSingleResponse(
                        krsId = krsId,
                        status = krsRow[KRSTable.status],
                        matakuliah = matkul
                    )
                }
            }
            // Kirim respons menggunakan data class KRSListResponse
            call.respond(KRSListResponse(success = true, krs = krsList))
        }
    }
}