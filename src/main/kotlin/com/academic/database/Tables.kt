package com.academic.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Base Person Table
object PersonTable : UUIDTable("persons") {
    val nama = varchar("nama", 100)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

// Mahasiswa Table (inherits from Person)
object MahasiswaTable : UUIDTable("mahasiswa") {
    val personId = reference("person_id", PersonTable)
    val nim = varchar("nim", 20).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
}

// Dosen Table (inherits from Person)
object DosenTable : UUIDTable("dosen") {
    val personId = reference("person_id", PersonTable)
    val nidn = varchar("nidn", 20).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
}

// Admin Table
object AdminTable : UUIDTable("admin") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val nama = varchar("nama", 100)
}

// Mata Kuliah Table
object MatakuliahTable : UUIDTable("matakuliah") {
    val nama = varchar("nama", 100)
    val sks = integer("sks")
    val ruangan = varchar("ruangan", 20)
    val jamMulai = varchar("jam_mulai", 10)
    val dosenId = reference("dosen_id", DosenTable)
}

// Junction table for Mahasiswa-Matakuliah (KRS)
object MahasiswaMatakuliahTable : UUIDTable("mahasiswa_matakuliah") {
    val mahasiswaId = reference("mahasiswa_id", MahasiswaTable)
    val matakuliahId = reference("matakuliah_id", MatakuliahTable)
    val tanggalAmbil = datetime("tanggal_ambil").default(LocalDateTime.now())
    
    init {
        uniqueIndex(mahasiswaId, matakuliahId)
    }
}

// Presensi Table
object PresensiTable : UUIDTable("presensi") {
    val mahasiswaId = reference("mahasiswa_id", MahasiswaTable)
    val matakuliahId = reference("matakuliah_id", MatakuliahTable)
    val presensi = enumeration("presensi", PresensiStatus::class)
    val tanggal = datetime("tanggal").default(LocalDateTime.now())
}

// Nilai Table
object NilaiTable : UUIDTable("nilai") {
    val mahasiswaId = reference("mahasiswa_id", MahasiswaTable)
    val matakuliahId = reference("matakuliah_id", MatakuliahTable)
    val nilai = enumeration("nilai", NilaiGrade::class)
    val tanggal = datetime("tanggal").default(LocalDateTime.now())
}

// Enums
enum class PresensiStatus {
    HADIR, SAKIT, IZIN, ALPHA
}

enum class NilaiGrade {
    A, B, C, D, E
}
