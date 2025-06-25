package com.academic.services

import com.academic.database.*
import com.academic.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthService {
    
    fun authenticateMahasiswa(identifier: String, password: String): Mahasiswa? {
        return transaction {
            val result = (MahasiswaTable innerJoin PersonTable)
                .select {
                    (MahasiswaTable.email eq identifier) or (MahasiswaTable.nim eq identifier)
                }
                .singleOrNull()
            
            result?.let {
                val hashedPassword = it[MahasiswaTable.passwordHash]
                if (BCrypt.checkpw(password, hashedPassword)) {
                    Mahasiswa(
                        id = it[MahasiswaTable.id].value.toString(),
                        nama = it[PersonTable.nama],
                        nim = it[MahasiswaTable.nim],
                        email = it[MahasiswaTable.email]
                    )
                } else null
            }
        }
    }
    
    fun authenticateDosen(identifier: String, password: String): Dosen? {
        return transaction {
            val result = (DosenTable innerJoin PersonTable)
                .select { DosenTable.nidn eq identifier }
                .singleOrNull()
            
            result?.let {
                val hashedPassword = it[DosenTable.passwordHash]
                if (BCrypt.checkpw(password, hashedPassword)) {
                    Dosen(
                        id = it[DosenTable.id].value.toString(),
                        nama = it[PersonTable.nama],
                        nidn = it[DosenTable.nidn]
                    )
                } else null
            }
        }
    }
    
    fun authenticateAdmin(identifier: String, password: String): Admin? {
        return transaction {
            val result = AdminTable
                .select { AdminTable.username eq identifier }
                .singleOrNull()
            
            result?.let {
                val hashedPassword = it[AdminTable.passwordHash]
                if (BCrypt.checkpw(password, hashedPassword)) {
                    Admin(
                        id = it[AdminTable.id].value.toString(),
                        username = it[AdminTable.username],
                        nama = it[AdminTable.nama]
                    )
                } else null
            }
        }
    }

    fun generateToken(user: Any, role: String): String {
        return when (user) {
            is Mahasiswa -> "mhs_${user.id}_${System.currentTimeMillis()}"
            is Dosen -> "dsn_${user.id}_${System.currentTimeMillis()}"
            is Admin -> "adm_${user.id}_${System.currentTimeMillis()}"
            else -> throw IllegalArgumentException("Invalid user type")
        }
    }

    fun validateToken(token: String): Pair<String, String>? {
        val parts = token.split("_")
        if (parts.size >= 3) {
            val role = when (parts[0]) {
                "mhs" -> "MAHASISWA"
                "dsn" -> "DOSEN"
                "adm" -> "ADMIN"
                else -> return null
            }
            return Pair(parts[1], role)
        }
        return null
    }
}
