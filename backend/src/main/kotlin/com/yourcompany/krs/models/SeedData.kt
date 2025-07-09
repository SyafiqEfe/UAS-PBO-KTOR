package com.yourcompany.krs.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object SeedData {
    fun insertInitialData() {
        transaction {
            SchemaUtils.create(MahasiswaTable, DosenTable, MataKuliahTable, KRSTable, KRSDetailTable, NilaiTable, AdminTable)

            // Admin default
            if (AdminTable.selectAll().empty()) {
                AdminTable.insert {
                    it[username] = "admin"
                    it[password] = "admin123" // hash in production
                }
            }

            // Dosen
            if (DosenTable.selectAll().empty()) {
                val dosenNames = listOf(
                    "Dr. Budi Santoso, M.Kom.",         // ID 1 - Ahli Algoritma & Mobile
                    "Dr. Retno Wulandari, M.T.",        // ID 2 - Ahli Struktur Data & IMK
                    "Prof. Eko Indrajit, M.Sc.",        // ID 3 - Ahli Basis Data & Sistem Informasi
                    "Dr. Dewi Lestari, S.T.",           // ID 4 - Ahli Jaringan & Grafika Komputer
                    "Ahmad Fauzi, M.Kom.",              // ID 5 - Ahli Sistem Operasi & Automata
                    "Siti Aminah, S.Kom., M.T.",        // ID 6 - Ahli Web
                    "Dr. Hendra Gunawan",               // ID 7 - Ahli AI
                    "Rina Marlina, M.Sc.",              // ID 8 - Ahli RPL
                    "Dr. Joko Susilo",                  // ID 9 - Ahli Matematika
                    "Lia Purnamasari, M.Kom."           // ID 10 - Ahli Analisis & Perancangan
                )

                dosenNames.forEachIndexed { index, dosenName ->
                    DosenTable.insert {
                        it[nidn] = "071205880${index + 1}"
                        it[nama] = dosenName
                        it[password] = "dosen${index + 1}"
                    }
                }
            }

            // Mata Kuliah
            if (MataKuliahTable.selectAll().empty()) {
                // --- MAPPING DOSEN DAN MATKUL DISESUAIKAN DI SINI ---
                val matkulList = listOf(
                    Triple("Algoritma dan Pemrograman", 3, 1),
                    Triple("Struktur Data", 3, 2),
                    Triple("Basis Data", 3, 3),
                    Triple("Jaringan Komputer", 3, 4),
                    Triple("Sistem Operasi", 3, 5),
                    Triple("Pemrograman Web", 3, 6),
                    Triple("Kecerdasan Buatan", 3, 7),
                    Triple("Rekayasa Perangkat Lunak", 3, 8),
                    Triple("Matematika Diskrit", 2, 9),
                    Triple("Analisis dan Perancangan Sistem", 3, 10),
                    Triple("Pemrograman Mobile", 3, 1), // Diajar oleh Dosen 1 lagi
                    Triple("Interaksi Manusia dan Komputer", 2, 2), // Diajar oleh Dosen 2 lagi
                    Triple("Sistem Informasi", 3, 3), // Diajar oleh Dosen 3 lagi
                    Triple("Grafika Komputer", 3, 4), // Diajar oleh Dosen 4 lagi
                    Triple("Teori Bahasa dan Automata", 2, 5)  // Diajar oleh Dosen 5 lagi
                )
                matkulList.forEachIndexed { idx, (nama, sks, dosenId) ->
                    MataKuliahTable.insert {
                        it[kode] = "IF%03d".format(idx + 1)
                        it[MataKuliahTable.nama] = nama
                        it[MataKuliahTable.sks] = sks
                        it[MataKuliahTable.dosenId] = dosenId // ID Dosen yang sudah disesuaikan
                        it[ruangan] = "Ruang %c%d".format('A' + (idx % 5), (idx % 3) + 1)
                        it[jamMulai] = "%02d:00".format(8 + (idx % 7))
                    }
                }
            }

            // Mahasiswa default
            if (MahasiswaTable.selectAll().empty()) {
                for (i in 1..5) {
                    MahasiswaTable.insert {
                        it[nim] = "9665223$i"
                        it[nama] = "Mahasiswa $i"
                        it[password] = "mahasiswa$i"
                        it[dpaId] = 1 // pastikan dosen dengan id 1 ada
                    }
                }
            }
        }
    }
}