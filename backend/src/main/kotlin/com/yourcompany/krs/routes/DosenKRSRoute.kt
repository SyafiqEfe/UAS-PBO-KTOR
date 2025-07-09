package com.yourcompany.krs.routes

import com.yourcompany.krs.models.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.dosenKRSRoute() {
    routing {
        // Endpoint utama untuk mengambil semua data dashboard dosen
        get("/dosen/{nidn}/dashboard") {
            val nidn = call.parameters["nidn"] ?: return@get call.respond(GenericResponse(false, "NIDN wajib diisi"))

            val dosenId = transaction {
                DosenTable.select { DosenTable.nidn eq nidn }.singleOrNull()?.get(DosenTable.id)?.value
            }
            if (dosenId == null) {
                return@get call.respond(GenericResponse(false, "Dosen tidak ditemukan"))
            }

            val matkulList = transaction {
                MataKuliahTable.select { MataKuliahTable.dosenId eq dosenId }.map { matkulRow ->
                    val matkulId = matkulRow[MataKuliahTable.id].value

                    // --- SINTAKS JOIN DIPERBAIKI DI SINI ---
                    val mahasiswaList = (KRSDetailTable innerJoin KRSTable innerJoin MahasiswaTable)
                        .join(
                            otherTable = NilaiTable,
                            joinType = JoinType.LEFT,
                            onColumn = KRSDetailTable.id,
                            otherColumn = NilaiTable.krsDetailId
                        )
                        .select { KRSDetailTable.matkulId eq matkulId }
                        .map { mhsRow ->
                            MahasiswaInClassResponse(
                                krsDetailId = mhsRow[KRSDetailTable.id].value,
                                nim = mhsRow[MahasiswaTable.nim],
                                nama = mhsRow[MahasiswaTable.nama],
                                nilai = mhsRow.getOrNull(NilaiTable.nilai),
                                keterangan = mhsRow.getOrNull(NilaiTable.keterangan)
                            )
                        }
                    
                    MatkulDosenResponse(
                        id = matkulId,
                        kode = matkulRow[MataKuliahTable.kode],
                        nama = matkulRow[MataKuliahTable.nama],
                        sks = matkulRow[MataKuliahTable.sks],
                        ruangan = matkulRow[MataKuliahTable.ruangan],
                        jamMulai = matkulRow[MataKuliahTable.jamMulai],
                        mahasiswa = mahasiswaList
                    )
                }
            }
            call.respond(matkulList)
        }

        // Endpoint untuk dosen mengubah jadwal matkul
        put("/dosen/matkul/{id}") {
            val matkulId = call.parameters["id"]?.toIntOrNull()
            val params = call.receiveParameters()
            val ruangan = params["ruangan"]
            val jamMulai = params["jamMulai"]

            if (matkulId == null || ruangan == null || jamMulai == null) {
                return@put call.respond(GenericResponse(false, "Parameter tidak lengkap"))
            }

            val updatedCount = transaction {
                MataKuliahTable.update({ MataKuliahTable.id eq matkulId }) {
                    it[MataKuliahTable.ruangan] = ruangan
                    it[MataKuliahTable.jamMulai] = jamMulai
                }
            }

            if (updatedCount > 0) {
                call.respond(GenericResponse(true, "Jadwal berhasil diperbarui"))
            } else {
                call.respond(GenericResponse(false, "Gagal memperbarui jadwal, mata kuliah tidak ditemukan"))
            }
        }
    }
}