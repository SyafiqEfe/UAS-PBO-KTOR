package com.academic.models

import kotlinx.serialization.Serializable
import java.util.*

// Mahasiswa class
@Serializable
data class Mahasiswa(
    val id: String,
    val nama: String,
    val nim: String,
    val email: String,
    val passwordHash: String? = null
)

// Dosen class
@Serializable
data class Dosen(
    val id: String,
    val nama: String,
    val nidn: String,
    val passwordHash: String? = null
)

// Admin class
@Serializable
data class Admin(
    val id: String,
    val username: String,
    val nama: String,
    val passwordHash: String? = null
)

// MataKuliah class
@Serializable
data class MataKuliah(
    val id: String,
    val nama: String,
    val sks: Int,
    val ruangan: String,
    val jamMulai: String,
    val dosenId: String,
    val dosenNama: String? = null,
    val jumlahMahasiswa: Int? = null
)

// Presensi class
@Serializable
data class Presensi(
    val id: String,
    val mahasiswaId: String,
    val matakuliahId: String,
    val presensi: String,
    val tanggal: String
)

// Nilai class
@Serializable
data class Nilai(
    val id: String,
    val mahasiswaId: String,
    val matakuliahId: String,
    val nilai: String,
    val tanggal: String
)

// Response classes
@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserInfo? = null
)

@Serializable
data class UserInfo(
    val id: String,
    val nama: String,
    val nim: String? = null,
    val nidn: String? = null,
    val username: String? = null,
    val email: String? = null,
    val role: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
