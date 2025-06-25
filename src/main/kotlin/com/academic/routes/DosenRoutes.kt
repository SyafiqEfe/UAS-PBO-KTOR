package com.academic.routes

import com.academic.database.*
import com.academic.dto.*
import com.academic.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.dosenRoutes() {
    authenticate("auth-bearer") {
        
        // Get mata kuliah yang diampu dosen
        get("/dosen/{nidn}/matkul") {
            try {
                val nidn = call.parameters["nidn"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIDN diperlukan", null))
                
                val matakuliah = transaction {
                    val dosen = DosenTable.select { DosenTable.nidn eq nidn }.singleOrNull()
                        ?: return@transaction emptyList<MataKuliah>()
                    
                    val dosenId = dosen[DosenTable.id].value
                    
                    (MatakuliahTable innerJoin DosenTable innerJoin PersonTable)
                        .select { MatakuliahTable.dosenId eq dosenId }
                        .map { row ->
                            // Count enrolled students
                            val jumlahMahasiswa = MahasiswaMatakuliahTable
                                .select { MahasiswaMatakuliahTable.matakuliahId eq row[MatakuliahTable.id].value }
                                .count().toInt()
                            
                            MataKuliah(
                                id = row[MatakuliahTable.id].value.toString(),
                                nama = row[MatakuliahTable.nama],
                                sks = row[MatakuliahTable.sks],
                                ruangan = row[MatakuliahTable.ruangan],
                                jamMulai = row[MatakuliahTable.jamMulai],
                                dosenId = row[MatakuliahTable.dosenId].value.toString(),
                                dosenNama = row[PersonTable.nama],
                                jumlahMahasiswa = jumlahMahasiswa
                            )
                        }
                }
                
                call.respond(matakuliah)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal memuat mata kuliah: ${e.message}", null))
            }
        }
        
        // Get mahasiswa per mata kuliah
        get("/dosen/{nidn}/matkul/{matkulId}/mahasiswa") {
            try {
                val nidn = call.parameters["nidn"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIDN diperlukan", null))
                
                val matkulId = call.parameters["matkulId"]?.let { UUID.fromString(it) }
                    ?: return@get call.respond(HttpStatusCode.BadRequest,
                        ApiResponse(false, "ID mata kuliah diperlukan", null))
                
                val mahasiswa = transaction {
                    // Verify dosen owns this mata kuliah
                    val dosen = DosenTable.select { DosenTable.nidn eq nidn }.singleOrNull()
                        ?: return@transaction emptyList<MahasiswaMatkulResponse>()
                    
                    val dosenId = dosen[DosenTable.id].value
                    
                    val matkul = MatakuliahTable.select { 
                        (MatakuliahTable.id eq matkulId) and (MatakuliahTable.dosenId eq dosenId) 
                    }.singleOrNull() ?: return@transaction emptyList<MahasiswaMatkulResponse>()
                    
                    // Get enrolled students
                    (MahasiswaMatakuliahTable innerJoin MahasiswaTable innerJoin PersonTable)
                        .select { MahasiswaMatakuliahTable.matakuliahId eq matkulId }
                        .map { row ->
                            val mahasiswaId = row[MahasiswaTable.id].value
                            
                            // Get latest presensi
                            val presensi = PresensiTable
                                .select { 
                                    (PresensiTable.mahasiswaId eq mahasiswaId) and 
                                    (PresensiTable.matakuliahId eq matkulId) 
                                }
                                .orderBy(PresensiTable.tanggal, SortOrder.DESC)
                                .limit(1)
                                .map { it[PresensiTable.presensi].name }
                                .firstOrNull()
                            
                            // Get nilai
                            val nilai = NilaiTable
                                .select { 
                                    (NilaiTable.mahasiswaId eq mahasiswaId) and 
                                    (NilaiTable.matakuliahId eq matkulId) 
                                }
                                .map { it[NilaiTable.nilai].name }
                                .firstOrNull()
                            
                          MahasiswaMatkulResponse(
    mahasiswaId = mahasiswaId.toString(),
    nama = row[PersonTable.nama],
    nim = row[MahasiswaTable.nim],
    nilai = nilai,
    presensi = presensi
)

                                                    }
                }
                
                call.respond(mahasiswa)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal memuat mahasiswa: ${e.message}", null))
            }
        }
        
        // Input presensi
        post("/dosen/{nidn}/matkul/{matkulId}/presensi") {
            try {
                val nidn = call.parameters["nidn"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIDN diperlukan", null))
                
                val matkulId = call.parameters["matkulId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest,
                        ApiResponse(false, "ID mata kuliah diperlukan", null))
                
                val request = call.receive<PresensiRequest>()
                val mahasiswaId = UUID.fromString(request.mahasiswaId)
                
                val result = transaction {
                    // Verify dosen owns this mata kuliah
                    val dosen = DosenTable.select { DosenTable.nidn eq nidn }.singleOrNull()
                        ?: return@transaction "Dosen tidak ditemukan"
                    
                    val dosenId = dosen[DosenTable.id].value
                    
                    val matkul = MatakuliahTable.select { 
                        (MatakuliahTable.id eq matkulId) and (MatakuliahTable.dosenId eq dosenId) 
                    }.singleOrNull() ?: return@transaction "Anda tidak mengampu mata kuliah ini"
                    
                    // Check if student is enrolled
                    val enrollment = MahasiswaMatakuliahTable.select {
                        (MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId) and
                        (MahasiswaMatakuliahTable.matakuliahId eq matkulId)
                    }.singleOrNull() ?: return@transaction "Mahasiswa tidak terdaftar di mata kuliah ini"
                    
                    // Insert or update presensi
                    val existing = PresensiTable.select {
                        (PresensiTable.mahasiswaId eq mahasiswaId) and
                        (PresensiTable.matakuliahId eq matkulId)
                    }.singleOrNull()
                    
                    if (existing != null) {
                        PresensiTable.update({
                            (PresensiTable.mahasiswaId eq mahasiswaId) and
                            (PresensiTable.matakuliahId eq matkulId)
                        }) {
                            it[presensi] = PresensiStatus.valueOf(request.presensi)
                        }
                    } else {
                        PresensiTable.insert {
                            it[PresensiTable.mahasiswaId] = mahasiswaId
                            it[PresensiTable.matakuliahId] = matkulId
                            it[presensi] = PresensiStatus.valueOf(request.presensi)
                        }
                    }
                    
                    "success"
                }
                
                if (result == "success") {
                    call.respond(ApiResponse(true, "Presensi berhasil disimpan", null))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, result, null))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal menyimpan presensi: ${e.message}", null))
            }
        }
        
        // Input nilai
        post("/dosen/{nidn}/matkul/{matkulId}/nilai") {
            try {
                val nidn = call.parameters["nidn"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIDN diperlukan", null))
                
                val matkulId = call.parameters["matkulId"]?.let { UUID.fromString(it) }
                    ?: return@post call.respond(HttpStatusCode.BadRequest,
                        ApiResponse(false, "ID mata kuliah diperlukan", null))
                
                val request = call.receive<NilaiRequest>()
                val mahasiswaId = UUID.fromString(request.mahasiswaId)
                
                val result = transaction {
                    // Verify dosen owns this mata kuliah
                    val dosen = DosenTable.select { DosenTable.nidn eq nidn }.singleOrNull()
                        ?: return@transaction "Dosen tidak ditemukan"
                    
                    val dosenId = dosen[DosenTable.id].value
                    
                    val matkul = MatakuliahTable.select { 
                        (MatakuliahTable.id eq matkulId) and (MatakuliahTable.dosenId eq dosenId) 
                    }.singleOrNull() ?: return@transaction "Anda tidak mengampu mata kuliah ini"
                    
                    // Check if student is enrolled
                    val enrollment = MahasiswaMatakuliahTable.select {
                        (MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId) and
                        (MahasiswaMatakuliahTable.matakuliahId eq matkulId)
                    }.singleOrNull() ?: return@transaction "Mahasiswa tidak terdaftar di mata kuliah ini"
                    
                    // Insert or update nilai
                    val existing = NilaiTable.select {
                        (NilaiTable.mahasiswaId eq mahasiswaId) and
                        (NilaiTable.matakuliahId eq matkulId)
                    }.singleOrNull()
                    
                    if (existing != null) {
                        NilaiTable.update({
                            (NilaiTable.mahasiswaId eq mahasiswaId) and
                            (NilaiTable.matakuliahId eq matkulId)
                        }) {
                            it[nilai] = NilaiGrade.valueOf(request.nilai)
                        }
                    } else {
                        NilaiTable.insert {
                            it[NilaiTable.mahasiswaId] = mahasiswaId
                            it[NilaiTable.matakuliahId] = matkulId
                            it[nilai] = NilaiGrade.valueOf(request.nilai)
                        }
                    }
                    
                    "success"
                }
                
                if (result == "success") {
                    call.respond(ApiResponse(true, "Nilai berhasil disimpan", null))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, result, null))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal menyimpan nilai: ${e.message}", null))
            }
        }
    }
}
