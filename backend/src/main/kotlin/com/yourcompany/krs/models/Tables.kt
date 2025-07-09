package com.yourcompany.krs.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object MahasiswaTable : IntIdTable("mahasiswa") {
    val nim = varchar("nim", 20).uniqueIndex()
    val nama = varchar("nama", 100)
    val password = varchar("password", 100)
    val dpaId = integer("dpa_id").nullable()
}

object DosenTable : IntIdTable("dosen") {
    val nidn = varchar("nidn", 20).uniqueIndex()
    val nama = varchar("nama", 100)
    val password = varchar("password", 100)
}

object MataKuliahTable : IntIdTable("matakuliah") {
    val kode = varchar("kode", 10).uniqueIndex()
    val nama = varchar("nama", 100)
    val sks = integer("sks")
    val dosenId = reference("dosen_id", DosenTable)
    val ruangan = varchar("ruangan", 20)
    val jamMulai = varchar("jam_mulai", 10)
}

object KRSTable : IntIdTable("krs") {
    val mahasiswaId = reference("mahasiswa_id", MahasiswaTable)
    val status = varchar("status", 20) // Draft, Diajukan, Disetujui, Ditolak
}

object KRSDetailTable : IntIdTable("krs_detail") {
    val krsId = reference("krs_id", KRSTable)
    val matkulId = reference("matkul_id", MataKuliahTable)
}

object NilaiTable : IntIdTable("nilai") {
    val krsDetailId = reference("krs_detail_id", KRSDetailTable)
    val nilai = varchar("nilai", 2).nullable()
    val keterangan = varchar("keterangan", 20).nullable() // Sakit, Izin, Alpha
}

object AdminTable : Table("admin") {
    val username = varchar("username", 20).uniqueIndex()
    val password = varchar("password", 100)
}
