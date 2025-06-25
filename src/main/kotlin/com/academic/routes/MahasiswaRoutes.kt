package com.academic.routes

import com.academic.database.*
import com.academic.dto.*
import com.academic.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.mahasiswaRoutes() {
    
    // Get all mata kuliah (public)
    get("/matakuliah") {
        try {
            val matakuliah = transaction {
                (MatakuliahTable innerJoin DosenTable innerJoin PersonTable)
                    .selectAll()
                    .map {
                        MataKuliah(
                            id = it[MatakuliahTable.id].value.toString(),
                            nama = it[MatakuliahTable.nama],
                            sks = it[MatakuliahTable.sks],
                            ruangan = it[MatakuliahTable.ruangan],
                            jamMulai = it[MatakuliahTable.jamMulai],
                            dosenId = it[MatakuliahTable.dosenId].value.toString(),
                            dosenNama = it[PersonTable.nama]
                        )
                    }
            }
            call.respond(matakuliah)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, 
                ApiResponse(false, "Gagal memuat mata kuliah: ${e.message}", null))
        }
    }
    
    authenticate("auth-bearer") {
        // Ambil mata kuliah
        post("/mahasiswa/{nim}/ambil-matkul") {
            try {
                val nim = call.parameters["nim"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIM diperlukan", null))
                
                val request = call.receive<AmbilMatkulRequest>()
                val matkulId = UUID.fromString(request.matkulId)
                
                val result = transaction {
                    // Get mahasiswa
                    val mahasiswa = MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()
                        ?: return@transaction "Mahasiswa tidak ditemukan"
                    
                    val mahasiswaId = mahasiswa[MahasiswaTable.id].value
                    
                    // Check if already enrolled
                    val existing = MahasiswaMatakuliahTable.select {
                        (MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId) and
                        (MahasiswaMatakuliahTable.matakuliahId eq matkulId)
                    }.singleOrNull()
                    
                    if (existing != null) {
                        return@transaction "Mata kuliah sudah diambil"
                    }
                    
                    // Check total SKS
                    val currentSKS = (MahasiswaMatakuliahTable innerJoin MatakuliahTable)
                        .select { MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId }
                        .sumOf { it[MatakuliahTable.sks] }
                    
                    val newMatkul = MatakuliahTable.select { MatakuliahTable.id eq matkulId }.single()
                    val newSKS = newMatkul[MatakuliahTable.sks]
                    
                    if (currentSKS + newSKS > 24) {
                        return@transaction "Total SKS tidak boleh melebihi 24"
                    }
                    
                    // Insert enrollment
                    MahasiswaMatakuliahTable.insert {
                        it[MahasiswaMatakuliahTable.mahasiswaId] = mahasiswaId
                        it[MahasiswaMatakuliahTable.matakuliahId] = matkulId
                    }
                    
                    "success"
                }
                
                if (result == "success") {
                    call.respond(ApiResponse(true, "Mata kuliah berhasil diambil", null))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, result, null))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Terjadi kesalahan: ${e.message}", null))
            }
        }
        
        // Drop mata kuliah
        delete("/mahasiswa/{nim}/drop-matkul") {
            try {
                val nim = call.parameters["nim"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIM diperlukan", null))
                
                val request = call.receive<AmbilMatkulRequest>()
                val matkulId = UUID.fromString(request.matkulId)
                
                val result = transaction {
                    val mahasiswa = MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()
                        ?: return@transaction "Mahasiswa tidak ditemukan"
                    
                    val mahasiswaId = mahasiswa[MahasiswaTable.id].value
                    
                    val deleted = MahasiswaMatakuliahTable.deleteWhere {
                        (MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId) and
                        (MahasiswaMatakuliahTable.matakuliahId eq matkulId)
                    }
                    
                    if (deleted > 0) "success" else "Mata kuliah tidak ditemukan"
                }
                
                if (result == "success") {
                    call.respond(ApiResponse(true, "Mata kuliah berhasil di-drop", null))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, result, null))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Terjadi kesalahan: ${e.message}", null))
            }
        }
        
        // Get jadwal mahasiswa
        get("/mahasiswa/{nim}/jadwal") {
            try {
                val nim = call.parameters["nim"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIM diperlukan", null))
                
                val jadwal = transaction {
                    val mahasiswa = MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()
                        ?: return@transaction emptyList<JadwalResponse>()
                    
                    val mahasiswaId = mahasiswa[MahasiswaTable.id].value
                    
                    (MahasiswaMatakuliahTable innerJoin MatakuliahTable innerJoin DosenTable innerJoin PersonTable)
                        .select { MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId }
                        .map {
                            JadwalResponse(
                                id = it[MatakuliahTable.id].value.toString(),
                                nama = it[MatakuliahTable.nama],
                                sks = it[MatakuliahTable.sks],
                                dosenNama = it[PersonTable.nama],
                                jamMulai = it[MatakuliahTable.jamMulai],
                                ruangan = it[MatakuliahTable.ruangan]
                            )
                        }
                }
                
                call.respond(jadwal)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal memuat jadwal: ${e.message}", null))
            }
        }
        
        // Get nilai dan presensi mahasiswa
        get("/mahasiswa/{nim}/nilai") {
            try {
                val nim = call.parameters["nim"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIM diperlukan", null))
                
                val nilaiPresensi = transaction {
                    val mahasiswa = MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()
                        ?: return@transaction emptyList<NilaiPresensiResponse>()
                    
                    val mahasiswaId = mahasiswa[MahasiswaTable.id].value
                    
                    // Get enrolled mata kuliah with nilai and presensi
                    (MahasiswaMatakuliahTable innerJoin MatakuliahTable innerJoin DosenTable innerJoin PersonTable)
                        .select { MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId }
                        .map { row ->
                            val matkulId = row[MatakuliahTable.id].value
                            
                            // Get presensi
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
                            
                            NilaiPresensiResponse(
                                matkulNama = row[MatakuliahTable.nama],
                                dosenNama = row[PersonTable.nama],
                                sks = row[MatakuliahTable.sks],
                                presensi = presensi,
                                nilai = nilai
                            )
                        }
                }
                
                call.respond(nilaiPresensi)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal memuat nilai: ${e.message}", null))
            }
        }
        
        // Update profil mahasiswa
        put("/mahasiswa/{nim}") {
            try {
                val nim = call.parameters["nim"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest, ApiResponse(false, "NIM diperlukan", null))
                
                val request = call.receive<UpdateMahasiswaRequest>()
                
                val updatedMahasiswa = transaction {
                    val mahasiswa = MahasiswaTable.select { MahasiswaTable.nim eq nim }.singleOrNull()
                        ?: return@transaction null
                    
                    val mahasiswaId = mahasiswa[MahasiswaTable.id].value
                    val personId = mahasiswa[MahasiswaTable.personId].value
                    
                    // Update person table
                    if (request.nama != null) {
                        PersonTable.update({ PersonTable.id eq personId }) {
                            it[nama] = request.nama
                        }
                    }
                    
                    // Update mahasiswa table
                    if (request.email != null || request.password != null) {
                        MahasiswaTable.update({ MahasiswaTable.id eq mahasiswaId }) {
                            if (request.email != null) it[email] = request.email
                            if (request.password != null) {
                                it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                            }
                        }
                    }
                    
                    // Return updated data
                    val updated = (MahasiswaTable innerJoin PersonTable)
                        .select { MahasiswaTable.id eq mahasiswaId }
                        .single()
                    
                    Mahasiswa(
                        id = updated[MahasiswaTable.id].value.toString(),
                        nama = updated[PersonTable.nama],
                        nim = updated[MahasiswaTable.nim],
                        email = updated[MahasiswaTable.email]
                    )
                }
                
                if (updatedMahasiswa != null) {
                    call.respond(updatedMahasiswa)
                } else {
                    call.respond(HttpStatusCode.NotFound,
                        ApiResponse(false, "Mahasiswa tidak ditemukan", null))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Gagal update profil: ${e.message}", null))
            }
        }
    }
}
