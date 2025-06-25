package com.academic.interfaces

import com.academic.models.Mahasiswa

// Interface Registrasi (as required by the assignment)
interface Registrasi {
    suspend fun registerMahasiswa(nama: String, email: String, password: String): Mahasiswa
    suspend fun validateRegistration(email: String): Boolean
    suspend fun generateNIM(): String
}
