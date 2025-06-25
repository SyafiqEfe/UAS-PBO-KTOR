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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.adminRoutes() {
    authenticate("auth-bearer") {
        
        // CRUD Mahasiswa
        route("/admin/mahasiswa") {
            
            // Get all mahasiswa
            get {
                try {
                    val mahasiswa = transaction {
                        (MahasiswaTable innerJoin PersonTable)
                            .selectAll()
                            .map { row ->
                                val mahasiswaId = row[MahasiswaTable.id].value
                                
                                // Calculate total SKS
                                val totalSKS = (MahasiswaMatakuliahTable innerJoin MatakuliahTable)
                                    .select { MahasiswaMatakuliahTable.mahasiswaId eq mahasiswaId }
                                    .sumOf { it[MatakuliahTable.sks] }
                                
                                mapOf(
                                    "id" to row[MahasiswaTable.id].value.toString(),
                                    "nim" to row[MahasiswaTable.nim],
                                    "nama" to row[PersonTable.nama],
                                    "email" to row[MahasiswaTable.email],
                                    "totalSKS" to totalSKS
                                )
                            }
                    }
                    call.respond(mahasiswa)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal memuat mahasiswa: ${e.message}", null))
                }
            }
            
            // Create mahasiswa
            post {
                try {
                    val request = call.receive<CreateMahasiswaRequest>()
                    
                    val mahasiswa = transaction {
                        // Check email uniqueness
                        val existing = MahasiswaTable.select { MahasiswaTable.email eq request.email }.singleOrNull()
                        if (existing != null) {
                            return@transaction null
                        }
                        
                        // Generate NIM
                        val currentYear = "23"
                        val lastNim = MahasiswaTable
                            .select { MahasiswaTable.nim like "$currentYear%" }
                            .orderBy(MahasiswaTable.nim, SortOrder.DESC)
                            .limit(1)
                            .map { it[MahasiswaTable.nim] }
                            .firstOrNull()
                        
                        val nextNumber = if (lastNim != null) {
                            val lastNumber = lastNim.substring(2).toInt()
                            lastNumber + 1
                        } else {
                            1
                        }
                        
                        val nim = "$currentYear${nextNumber.toString().padStart(4, '0')}"
                        
                        // Insert Person
                        val personId = PersonTable.insertAndGetId {
                            it[nama] = request.nama
                        }
                        
                        // Insert Mahasiswa
                        val mahasiswaId = MahasiswaTable.insertAndGetId {
                            it[MahasiswaTable.personId] = personId
                            it[MahasiswaTable.nim] = nim
                            it[email] = request.email
                            it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                        }
                        
                        Mahasiswa(
                            id = mahasiswaId.value.toString(),
                            nama = request.nama,
                            nim = nim,
                            email = request.email
                        )
                    }
                    
                    if (mahasiswa != null) {
                        call.respond(HttpStatusCode.Created, mahasiswa)
                    } else {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "Email sudah terdaftar", null))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal membuat mahasiswa: ${e.message}", null))
                }
            }
            
            // Delete mahasiswa
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "ID mahasiswa diperlukan", null))
                    
                    val deleted = transaction {
                        val mahasiswa = MahasiswaTable.select { MahasiswaTable.id eq id }.singleOrNull()
                            ?: return@transaction false
                        
                        val personId = mahasiswa[MahasiswaTable.personId].value
                        
                        // Delete related records first
                        MahasiswaMatakuliahTable.deleteWhere { MahasiswaMatakuliahTable.mahasiswaId eq id }
                        PresensiTable.deleteWhere { PresensiTable.mahasiswaId eq id }
                        NilaiTable.deleteWhere { NilaiTable.mahasiswaId eq id }
                        
                        // Delete mahasiswa
                        MahasiswaTable.deleteWhere { MahasiswaTable.id eq id }
                        
                        // Delete person
                        PersonTable.deleteWhere { PersonTable.id eq personId }
                        
                        true
                    }
                    
                    if (deleted) {
                        call.respond(ApiResponse(true, "Mahasiswa berhasil dihapus", null))
                    } else {
                        call.respond(HttpStatusCode.NotFound,
                            ApiResponse(false, "Mahasiswa tidak ditemukan", null))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal menghapus mahasiswa: ${e.message}", null))
                }
            }
        }
        
        // CRUD Dosen
        route("/admin/dosen") {
            
            // Get all dosen
            get {
                try {
                    val dosen = transaction {
                        (DosenTable innerJoin PersonTable)
                            .selectAll()
                            .map { row ->
                                val dosenId = row[DosenTable.id].value
                                
                                // Get mata kuliah yang diampu
                                val matakuliah = MatakuliahTable
                                    .select { MatakuliahTable.dosenId eq dosenId }
                                    .map { it[MatakuliahTable.nama] }
                                
                                mapOf(
                                    "id" to row[DosenTable.id].value.toString(),
                                    "nidn" to row[DosenTable.nidn],
                                    "nama" to row[PersonTable.nama],
                                    "matakuliah" to matakuliah
                                )
                            }
                    }
                    call.respond(dosen)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal memuat dosen: ${e.message}", null))
                }
            }
            
            // Create dosen
            post {
                try {
                    val request = call.receive<CreateDosenRequest>()
                    
                    val dosen = transaction {
                        // Check NIDN uniqueness
                        val existing = DosenTable.select { DosenTable.nidn eq request.nidn }.singleOrNull()
                        if (existing != null) {
                            return@transaction null
                        }
                        
                        // Insert Person
                        val personId = PersonTable.insertAndGetId {
                            it[nama] = request.nama
                        }
                        
                        // Insert Dosen
                        val dosenId = DosenTable.insertAndGetId {
                            it[DosenTable.personId] = personId
                            it[nidn] = request.nidn
                            it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                        }
                        
                        Dosen(
                            id = dosenId.value.toString(),
                            nama = request.nama,
                            nidn = request.nidn
                        )
                    }
                    
                    if (dosen != null) {
                        call.respond(HttpStatusCode.Created, dosen)
                    } else {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "NIDN sudah terdaftar", null))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal membuat dosen: ${e.message}", null))
                }
            }
            
            // Delete dosen
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "ID dosen diperlukan", null))
                    
                    val deleted = transaction {
                        val dosen = DosenTable.select { DosenTable.id eq id }.singleOrNull()
                            ?: return@transaction false
                        
                        val personId = dosen[DosenTable.personId].value
                        
                        // Check if dosen has mata kuliah
                        val hasMatkul = MatakuliahTable.select { MatakuliahTable.dosenId eq id }.count() > 0
                        if (hasMatkul) {
                            return@transaction false
                        }
                        
                        // Delete dosen
                        DosenTable.deleteWhere { DosenTable.id eq id }
                        
                        // Delete person
                        PersonTable.deleteWhere { PersonTable.id eq personId }
                        
                        true
                    }
                    
                    if (deleted) {
                        call.respond(ApiResponse(true, "Dosen berhasil dihapus", null))
                    } else {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "Tidak dapat menghapus dosen yang masih mengampu mata kuliah", null))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal menghapus dosen: ${e.message}", null))
                }
            }
        }
        
        // CRUD Mata Kuliah
        route("/admin/matakuliah") {
            
            // Get all mata kuliah
            get {
                try {
                    val matakuliah = transaction {
                        (MatakuliahTable innerJoin DosenTable innerJoin PersonTable)
                            .selectAll()
                            .map { row ->
                                val matkulId = row[MatakuliahTable.id].value
                                
                                // Count enrolled students
                                val jumlahMahasiswa = MahasiswaMatakuliahTable
                                    .select { MahasiswaMatakuliahTable.matakuliahId eq matkulId }
                                    .count().toInt()
                                
                                mapOf(
                                    "id" to row[MatakuliahTable.id].value.toString(),
                                    "nama" to row[MatakuliahTable.nama],
                                    "sks" to row[MatakuliahTable.sks],
                                    "dosenNama" to row[PersonTable.nama],
                                    "jamMulai" to row[MatakuliahTable.jamMulai],
                                    "ruangan" to row[MatakuliahTable.ruangan],
                                    "jumlahMahasiswa" to jumlahMahasiswa
                                )
                            }
                    }
                    call.respond(matakuliah)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal memuat mata kuliah: ${e.message}", null))
                }
            }
            
            // Create mata kuliah
            post {
                try {
                    val request = call.receive<CreateMatakuliahRequest>()
                    
                    val matakuliah = transaction {
                        val dosenId = UUID.fromString(request.dosenId)
                        
                        // Verify dosen exists
                        val dosen = DosenTable.select { DosenTable.id eq dosenId }.singleOrNull()
                            ?: return@transaction null
                        
                        // Insert mata kuliah
                        val matkulId = MatakuliahTable.insertAndGetId {
                            it[nama] = request.nama
                            it[sks] = request.sks
                            it[MatakuliahTable.dosenId] = dosenId
                            it[jamMulai] = request.jamMulai
                            it[ruangan] = request.ruangan
                        }
                        
                        // Get dosen name
                        val dosenNama = (DosenTable innerJoin PersonTable)
                            .select { DosenTable.id eq dosenId }
                            .single()[PersonTable.nama]
                        
                        MataKuliah(
                            id = matkulId.value.toString(),
                            nama = request.nama,
                            sks = request.sks,
                            ruangan = request.ruangan,
                            jamMulai = request.jamMulai,
                            dosenId = request.dosenId,
                            dosenNama = dosenNama
                        )
                    }
                    
                    if (matakuliah != null) {
                        call.respond(HttpStatusCode.Created, matakuliah)
                    } else {
                        call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "Dosen tidak ditemukan", null))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal membuat mata kuliah: ${e.message}", null))
                }
            }
            
            // Delete mata kuliah
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest,
                            ApiResponse(false, "ID mata kuliah diperlukan", null))
                    
                    val deleted = transaction {
                        // Delete related records first
                        MahasiswaMatakuliahTable.deleteWhere { MahasiswaMatakuliahTable.matakuliahId eq id }
                        PresensiTable.deleteWhere { PresensiTable.matakuliahId eq id }
                        NilaiTable.deleteWhere { NilaiTable.matakuliahId eq id }
                        
                        // Delete mata kuliah
                        val deletedCount = MatakuliahTable.deleteWhere { MatakuliahTable.id eq id }
                        deletedCount > 0
                    }
                    
                    if (deleted) {
                        call.respond(ApiResponse(true, "Mata kuliah berhasil dihapus", null))
                    } else {
                        call.respond(HttpStatusCode.NotFound,
                            ApiResponse(false, "Mata kuliah tidak ditemukan", null))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError,
                        ApiResponse(false, "Gagal menghapus mata kuliah: ${e.message}", null))
                }
            }
        }
    }
}
