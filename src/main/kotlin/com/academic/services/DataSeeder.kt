package com.academic.services

import com.academic.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.util.*

object DataSeeder {
    fun seedData() {
        transaction {
            // Cegah duplikat seed
            if (AdminTable.selectAll().count() > 0) return@transaction

            // Admin
            AdminTable.insert {
                it[username] = "admin"
                it[passwordHash] = BCrypt.hashpw("admin123", BCrypt.gensalt())
                it[nama] = "Administrator"
            }

            // Seed Dosen dan Person
            val dosenData = listOf(
                Triple("100001", "Dr. Ahmad Fikri", "fikri123"),
                Triple("100002", "Dr. Lestari Ayu", "lestari1"),
                Triple("100003", "Prof. Budi Santosa", "budi2025"),
                Triple("100004", "Dr. Sari Indah", "sari456"),
                Triple("100005", "Prof. Joko Widodo", "joko789")
            )

            val dosenIds = mutableMapOf<String, UUID>()

            dosenData.forEach { (nidn, nama, password) ->
                val personId = PersonTable.insertAndGetId {
                    it[PersonTable.nama] = nama
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }

                val dosenId = DosenTable.insertAndGetId {
                    it[DosenTable.personId] = personId
                    it[DosenTable.nidn] = nidn
                    it[DosenTable.passwordHash] = BCrypt.hashpw(password, BCrypt.gensalt())
                }

                dosenIds[nidn] = dosenId.value
            }

            // Seed Mahasiswa dan Person
            val mahasiswaPersonId = UUID.randomUUID()
            val mahasiswaId = UUID.randomUUID()

            PersonTable.insert {
                it[id] = mahasiswaPersonId
                it[nama] = "Andi Pratama"
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }

            MahasiswaTable.insert {
                it[id] = mahasiswaId
                it[personId] = mahasiswaPersonId
                it[nim] = "2200112233"
                it[email] = "andi@example.com"
                it[passwordHash] = BCrypt.hashpw("mahasiswa123", BCrypt.gensalt())
            }

            // Mata Kuliah
            val matkulData = listOf(
                Tuple5("Pemrograman Web", 3, "100001", "08:00", "A101"),
                Tuple5("Struktur Data", 4, "100002", "10:00", "B203"),
                Tuple5("Algoritma dan Pemrograman", 4, "100003", "13:00", "C302"),
                Tuple5("Jaringan Komputer", 3, "100001", "15:00", "A105"),
                Tuple5("Sistem Operasi", 4, "100003", "07:00", "C101"),
                Tuple5("Database Management", 3, "100002", "09:00", "B201"),
                Tuple5("Pemrograman Mobile", 3, "100004", "11:00", "A103"),
                Tuple5("Machine Learning", 4, "100005", "14:00", "C201")
            )

            matkulData.forEach { (nama, sks, dosenNidn, jam, ruangan) ->
                val dosenId = dosenIds[dosenNidn]!!
                MatakuliahTable.insert {
                    it[MatakuliahTable.nama] = nama
                    it[MatakuliahTable.sks] = sks
                    it[MatakuliahTable.dosenId] = dosenId
                    it[MatakuliahTable.jamMulai] = jam
                    it[MatakuliahTable.ruangan] = ruangan
                }
            }
        }
    }
}

data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
