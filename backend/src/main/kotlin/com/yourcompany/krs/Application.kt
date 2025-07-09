package com.yourcompany.krs

import com.yourcompany.krs.models.DatabaseFactory
import com.yourcompany.krs.models.SeedData
import com.yourcompany.krs.plugins.configureCORS
import com.yourcompany.krs.routes.adminRoute
import com.yourcompany.krs.routes.authRoute
import com.yourcompany.krs.routes.dosenKRSRoute
import com.yourcompany.krs.routes.dosenRoute
import com.yourcompany.krs.routes.healthRoute
import com.yourcompany.krs.routes.krsRoute
import com.yourcompany.krs.routes.mahasiswaRoute
import com.yourcompany.krs.routes.matakuliahRoute // ✅ 1. IMPORT DITAMBAHKAN
import com.yourcompany.krs.routes.nilaiRoute
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    DatabaseFactory.init()
    SeedData.insertInitialData()
    install(ContentNegotiation) {
        json()
    }
    configureCORS()
    healthRoute()
    authRoute()
    mahasiswaRoute()
    dosenRoute()
    adminRoute()
    krsRoute()
    nilaiRoute()
    dosenKRSRoute()
    
    matakuliahRoute() // ✅ 2. RUTE BARU DIPANGGIL DI SINI
}