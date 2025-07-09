package com.yourcompany.krs.routes

import com.yourcompany.krs.models.DosenTable
import com.yourcompany.krs.models.MataKuliahTable
import com.yourcompany.krs.models.MatakuliahResponse // -> IMPORT BARU
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.matakuliahRoute() {
    routing {
        get("/matakuliah") {
            val matakuliahList = transaction {
                MataKuliahTable.selectAll().map {
                    val dosenNama = DosenTable
                        .select { DosenTable.id eq it[MataKuliahTable.dosenId] }
                        .singleOrNull()
                        ?.get(DosenTable.nama) ?: "N/A"
                    
                    // 👇 GANTI mapOf DENGAN DATA CLASS
                    MatakuliahResponse(
                        id = it[MataKuliahTable.id].value,
                        kode = it[MataKuliahTable.kode],
                        nama = it[MataKuliahTable.nama],
                        sks = it[MataKuliahTable.sks],
                        dosen = dosenNama,
                        ruangan = it[MataKuliahTable.ruangan],
                        jamMulai = it[MataKuliahTable.jamMulai]
                    )
                }
            }
            // 👇 KIRIM LIST LANGSUNG
            call.respond(matakuliahList)
        }
    }
}