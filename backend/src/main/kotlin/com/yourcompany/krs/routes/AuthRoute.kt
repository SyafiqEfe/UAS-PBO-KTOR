package com.yourcompany.krs.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import com.yourcompany.krs.models.*
import org.jetbrains.exposed.dao.id.EntityID

fun Application.authRoute() {
    routing {
        post("/login") {
            val params = call.receiveParameters()
            val username = params["username"] ?: ""
            val password = params["password"] ?: ""
            val role = params["role"] ?: "mahasiswa"
            var result: String? = null
            var id: Int? = null
            transaction {
                when (role) {
                    "admin" -> {
                        val row = AdminTable.select { AdminTable.username eq username and (AdminTable.password eq password) }.singleOrNull()
                        result = row?.get(AdminTable.username)
                    }
                    "dosen" -> {
                        val row = DosenTable.select { DosenTable.nidn eq username and (DosenTable.password eq password) }.singleOrNull()
                        result = row?.get(DosenTable.nidn)
                        id = row?.get(DosenTable.id)?.value
                    }
                    else -> {
                        val row = MahasiswaTable.select { MahasiswaTable.nim eq username and (MahasiswaTable.password eq password) }.singleOrNull()
                        result = row?.get(MahasiswaTable.nim)
                        id = row?.get(MahasiswaTable.id)?.value
                    }
                }
            }
            if (result != null) {
                call.respond(SimpleResponse(success = true, role = role, username = result, id = id))
            } else {
                call.respond(SimpleResponse(success = false, message = "Login gagal"))
            }
        }
        // CRUD Mahasiswa
        post("/admin/mahasiswa") {
            val params = call.receiveParameters()
            val nim = params["nim"] ?: return@post call.respond(SimpleResponse(success = false, message = "NIM wajib diisi"))
            val nama = params["nama"] ?: return@post call.respond(SimpleResponse(success = false, message = "Nama wajib diisi"))
            val password = params["password"] ?: "123456"
            val dpaId = params["dpaId"]?.toIntOrNull()
            val existing = transaction {
                MahasiswaTable.select { MahasiswaTable.nim eq nim }.count()
            }
            if (existing > 0) {
                return@post call.respond(SimpleResponse(success = false, message = "NIM sudah terdaftar"))
            }
            transaction {
                MahasiswaTable.insert {
                    it[MahasiswaTable.nim] = nim
                    it[MahasiswaTable.nama] = nama
                    it[MahasiswaTable.password] = password
                    it[MahasiswaTable.dpaId] = dpaId // langsung integer nullable
                }
            }
            call.respond(SimpleResponse(success = true, nim = nim))
        }
        put("/admin/mahasiswa/{nim}") {
            val nim = call.parameters["nim"] ?: return@put call.respond(SimpleResponse(success = false, message = "NIM wajib diisi"))
            val params = call.receiveParameters()
            transaction {
                MahasiswaTable.update({ MahasiswaTable.nim eq nim }) {
                    if (params["nama"] != null) it[nama] = params["nama"]!!
                    if (params["password"] != null) it[password] = params["password"]!!
                    if (params["dpaId"] != null) it[dpaId] = params["dpaId"]?.toIntOrNull()
                }
            }
            call.respond(SimpleResponse(success = true))
        }
        delete("/admin/mahasiswa/{nim}") {
            val nim = call.parameters["nim"] ?: return@delete call.respond(SimpleResponse(success = false, message = "NIM wajib diisi"))
            transaction {
                MahasiswaTable.deleteWhere { MahasiswaTable.nim eq nim }
            }
            call.respond(SimpleResponse(success = true))
        }
        // CRUD Dosen
        post("/admin/dosen") {
            val params = call.receiveParameters()
            val nidn = params["nidn"] ?: return@post call.respond(mapOf("success" to false, "message" to "NIDN wajib diisi"))
            val nama = params["nama"] ?: return@post call.respond(mapOf("success" to false, "message" to "Nama wajib diisi"))
            val password = params["password"] ?: "123456"
            transaction {
                DosenTable.insert {
                    it[DosenTable.nidn] = nidn
                    it[DosenTable.nama] = nama
                    it[DosenTable.password] = password
                }
            }
            call.respond(mapOf("success" to true))
        }
        put("/admin/dosen/{nidn}") {
            val nidn = call.parameters["nidn"] ?: return@put call.respond(mapOf("success" to false, "message" to "NIDN wajib diisi"))
            val params = call.receiveParameters()
            transaction {
                DosenTable.update({ DosenTable.nidn eq nidn }) {
                    if (params["nama"] != null) it[nama] = params["nama"]!!
                    if (params["password"] != null) it[password] = params["password"]!!
                }
            }
            call.respond(mapOf("success" to true))
        }
        delete("/admin/dosen/{nidn}") {
            val nidn = call.parameters["nidn"] ?: return@delete call.respond(mapOf("success" to false, "message" to "NIDN wajib diisi"))
            transaction {
                DosenTable.deleteWhere { DosenTable.nidn eq nidn }
            }
            call.respond(mapOf("success" to true))
        }
        // CRUD Matakuliah
        post("/admin/matakuliah") {
            val params = call.receiveParameters()
            val kode = params["kode"] ?: return@post call.respond(mapOf("success" to false, "message" to "Kode wajib diisi"))
            val nama = params["nama"] ?: return@post call.respond(mapOf("success" to false, "message" to "Nama wajib diisi"))
            val sks = params["sks"]?.toIntOrNull() ?: return@post call.respond(mapOf("success" to false, "message" to "SKS wajib diisi"))
            val dosenId = params["dosenId"]?.toIntOrNull() ?: return@post call.respond(mapOf("success" to false, "message" to "Dosen wajib diisi"))
            val ruangan = params["ruangan"] ?: "-"
            val jamMulai = params["jamMulai"] ?: "-"
            transaction {
                MataKuliahTable.insert {
                    it[MataKuliahTable.kode] = kode
                    it[MataKuliahTable.nama] = nama
                    it[MataKuliahTable.sks] = sks
                    it[MataKuliahTable.dosenId] = org.jetbrains.exposed.dao.id.EntityID(dosenId, DosenTable)
                    it[MataKuliahTable.ruangan] = ruangan
                    it[MataKuliahTable.jamMulai] = jamMulai
                }
            }
            call.respond(mapOf("success" to true))
        }
        put("/admin/matakuliah/{kode}") {
            val kode = call.parameters["kode"] ?: return@put call.respond(mapOf("success" to false, "message" to "Kode wajib diisi"))
            val params = call.receiveParameters()
            transaction {
                MataKuliahTable.update({ MataKuliahTable.kode eq kode }) {
                    if (params["nama"] != null) it[MataKuliahTable.nama] = params["nama"]!!
                    params["sks"]?.toIntOrNull()?.let { value -> it[MataKuliahTable.sks] = value }
                    params["dosenId"]?.toIntOrNull()?.let { id ->
                        it[MataKuliahTable.dosenId] = org.jetbrains.exposed.dao.id.EntityID(id, DosenTable)
                    }
                    if (params["ruangan"] != null) it[MataKuliahTable.ruangan] = params["ruangan"]!!
                    if (params["jamMulai"] != null) it[MataKuliahTable.jamMulai] = params["jamMulai"]!!
                }
            }
            call.respond(mapOf("success" to true))
        }
        delete("/admin/matakuliah/{kode}") {
            val kode = call.parameters["kode"] ?: return@delete call.respond(mapOf("success" to false, "message" to "Kode wajib diisi"))
            transaction {
                MataKuliahTable.deleteWhere { MataKuliahTable.kode eq kode }
            }
            call.respond(mapOf("success" to true))
        }
    }
}
