package com.academic.plugins

import com.academic.database.*
import com.academic.services.DataSeeder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = HikariConfig().apply {
        driverClassName = "org.sqlite.JDBC"
        jdbcUrl = "jdbc:sqlite:data/academic.db" // File SQLite disimpan di folder /data
        maximumPoolSize = 1 // SQLite tidak mendukung koneksi paralel tinggi
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_SERIALIZABLE"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    // Buat tabel jika belum ada
    transaction {
        SchemaUtils.create(
            PersonTable,
            MahasiswaTable,
            DosenTable,
            AdminTable,
            MatakuliahTable,
            MahasiswaMatakuliahTable,
            PresensiTable,
            NilaiTable
        )
    }

    // Seed data awal (Admin, Dosen, Mahasiswa, Matakuliah)
    DataSeeder.seedData()
}
