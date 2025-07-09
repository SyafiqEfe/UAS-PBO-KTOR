package com.yourcompany.krs.models

import kotlinx.serialization.Serializable

@Serializable
data class GenericResponse(val success: Boolean, val message: String)

@Serializable
data class RegisterSuccessResponse(val success: Boolean, val nim: String)

// --- DATA CLASS BARU UNTUK RESPONS ADMIN ---

@Serializable
data class MahasiswaResponse( // Ini untuk satu objek mahasiswa yang dikirim
    val id: Int,
    val nim: String,
    val nama: String,
    val dpaId: Int? // dpaId bisa null
)

@Serializable
data class MahasiswaListResponse(
    val success: Boolean,
    val mahasiswa: List<MahasiswaResponse>
)

@Serializable
data class DosenResponse( // Ini untuk satu objek dosen yang dikirim
    val id: Int, // Pastikan ID ini ada di DosenTable dan diambil
    val nidn: String,
    val nama: String
)

@Serializable
data class DosenListResponse(
    val success: Boolean,
    val dosen: List<DosenResponse>
)

@Serializable
data class MatakuliahAdminResponse( // Mata kuliah yang dikirim dari admin endpoint
    val id: Int,
    val kode: String,
    val nama: String,
    val sks: Int,
    val dosenId: Int, // ID dosen
    val dosen: String, // Nama dosen
    val ruangan: String,
    val jamMulai: String
)

@Serializable
data class MatakuliahListAdminResponse(
    val success: Boolean,
    val matakuliah: List<MatakuliahAdminResponse>
)

// --- END DATA CLASS BARU ---


@Serializable
data class MatakuliahResponse(
    val id: Int,
    val kode: String,
    val nama: String,
    val sks: Int,
    val dosen: String,
    val ruangan: String,
    val jamMulai: String
)

@Serializable
data class KRSSingleResponse(
    val krsId: Int,
    val status: String,
    val matakuliah: List<MatakuliahResponse>
)

@Serializable
data class KRSListResponse(
    val success: Boolean,
    val krs: List<KRSSingleResponse>
)

@Serializable
data class NilaiResponse(
    val matakuliah: String,
    val sks: Int,
    val dosen: String,
    val nilai: String?,
    val keterangan: String?
)

@Serializable
data class NilaiListResponse(
    val success: Boolean,
    val nilai: List<NilaiResponse>
)

@Serializable
data class MahasiswaInClassResponse(
    val krsDetailId: Int,
    val nim: String,
    val nama: String,
    val nilai: String?,
    val keterangan: String?
)

@Serializable
data class MatkulDosenResponse(
    val id: Int,
    val kode: String,
    val nama: String,
    val sks: Int,
    val ruangan: String,
    val jamMulai: String,
    val mahasiswa: List<MahasiswaInClassResponse>
)