package com.yourcompany.krs.routes

import com.yourcompany.krs.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.nilaiRoute() {
    routing {
        post("/nilai/input") {
            val params = call.receiveParameters()
            val krsDetailId = params["krsDetailId"]?.toIntOrNull()
            val nilai = params["nilai"]
            val keterangan = params["keterangan"]

            if (krsDetailId == null || nilai == null || keterangan == null) {
                return@post call.respond(GenericResponse(false, "Parameter tidak lengkap"))
            }

            transaction {
                NilaiTable.deleteWhere { NilaiTable.krsDetailId eq krsDetailId }
                NilaiTable.insert {
                    it[NilaiTable.krsDetailId] = krsDetailId
                    it[NilaiTable.nilai] = nilai
                    it[NilaiTable.keterangan] = keterangan
                }
            }
            call.respond(GenericResponse(true, "Nilai berhasil diinput"))
        }

        get("/nilai/{nim}") {
            val nim = call.parameters["nim"] ?: return@get call.respond(GenericResponse(false, "NIM tidak ada"))

            val mahasiswaId = transaction {
                MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()?.get(MahasiswaTable.id)
            }
            if (mahasiswaId == null) {
                return@get call.respond(GenericResponse(false, "Mahasiswa tidak ditemukan"))
            }

            val nilaiList = transaction {
                // --- INI ADALAH QUERY DENGAN SINTAKS JOIN YANG BENAR ---
                val query = (KRSTable innerJoin KRSDetailTable innerJoin MataKuliahTable innerJoin DosenTable)
                    .join(
                        otherTable = NilaiTable,
                        joinType = JoinType.LEFT,
                        onColumn = KRSDetailTable.id,
                        otherColumn = NilaiTable.krsDetailId
                    )
                
                query.select { KRSTable.mahasiswaId eq mahasiswaId.value }
                    .map {
                        NilaiResponse(
                            matakuliah = it[MataKuliahTable.nama],
                            sks = it[MataKuliahTable.sks],
                            dosen = it[DosenTable.nama],
                            nilai = it.getOrNull(NilaiTable.nilai),
                            keterangan = it.getOrNull(NilaiTable.keterangan)
                        )
                    }
            }
            call.respond(NilaiListResponse(success = true, nilai = nilaiList))
        }
    }
}