package com.academic.services

import com.academic.database.*
import com.academic.interfaces.Registrasi
import com.academic.models.Mahasiswa
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

// RegistrasiService implements Registrasi interface
class RegistrasiService : Registrasi {
    
    override suspend fun registerMahasiswa(nama: String, email: String, password: String): Mahasiswa {
        return transaction {
            // Validate email uniqueness
            if (!validateRegistrationSync(email)) {
                throw IllegalArgumentException("Email sudah terdaftar")
            }
            
            // Generate NIM
            val nim = generateNIMSync()
            
            // Hash password
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            
            // Insert into Person table first
            val personId = PersonTable.insertAndGetId {
                it[PersonTable.nama] = nama
            }
            
            // Insert into Mahasiswa table
            val mahasiswaId = MahasiswaTable.insertAndGetId {
                it[MahasiswaTable.personId] = personId
                it[MahasiswaTable.nim] = nim
                it[MahasiswaTable.email] = email
                it[MahasiswaTable.passwordHash] = hashedPassword
            }
            
            Mahasiswa(
                id = mahasiswaId.value.toString(),
                nama = nama,
                nim = nim,
                email = email
            )
        }
    }
    
    override suspend fun validateRegistration(email: String): Boolean {
        return transaction {
            validateRegistrationSync(email)
        }
    }
    
    override suspend fun generateNIM(): String {
        return transaction {
            generateNIMSync()
        }
    }
    
    private fun validateRegistrationSync(email: String): Boolean {
        return MahasiswaTable.select { MahasiswaTable.email eq email }.empty()
    }
    
    private fun generateNIMSync(): String {
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
        
        return "$currentYear${nextNumber.toString().padStart(4, '0')}"
    }
}
