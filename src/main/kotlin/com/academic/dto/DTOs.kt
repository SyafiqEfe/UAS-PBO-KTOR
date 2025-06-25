package com.academic.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateMahasiswaRequest(
    val nama: String,
    val email: String,
    val password: String
)

@Serializable
data class CreateDosenRequest(
    val nama: String,
    val nidn: String,
    val password: String
)

@Serializable
data class CreateMatakuliahRequest(
    val nama: String,
    val sks: Int,
    val dosenId: String,
    val jamMulai: String,
    val ruangan: String
)

@Serializable
data class AmbilMatkulRequest(
    val matkulId: String
)

@Serializable
data class UpdateMahasiswaRequest(
    val nama: String? = null,
    val email: String? = null,
    val password: String? = null
)

@Serializable
data class JadwalResponse(
    val id: String,
    val nama: String,
    val sks: Int,
    val dosenNama: String,
    val jamMulai: String,
    val ruangan: String
)

@Serializable
data class NilaiPresensiResponse(
    val matkulNama: String,
    val dosenNama: String,
    val sks: Int,
    val presensi: String?,
    val nilai: String?
)

@Serializable
data class LoginRequest(
    val identifier: String, // bisa NIM / NIDN / username
    val password: String,
    val role: String // MAHASISWA, DOSEN, ADMIN
)


@Serializable
data class UpdatePresensiRequest(
    val mahasiswaId: String,
    val presensi: String // HADIR, TIDAK_HADIR, IZIN, SAKIT
)

@Serializable
data class UpdateNilaiRequest(
    val mahasiswaId: String,
    val nilai: String // A, B, C, D, E
)

@Serializable
data class RegisterRequest(
    val nama: String,
    val email: String,
    val password: String
)

@Serializable
data class MahasiswaMatkulResponse(
    val mahasiswaId: String,
    val nama: String,
    val nim: String,
    val nilai: String?,
    val presensi: String?
)

@Serializable
data class PresensiRequest(
    val mahasiswaId: String,
    val presensi: String
)

@Serializable
data class NilaiRequest(
    val mahasiswaId: String,
    val nilai: String
)
