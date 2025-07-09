package com.yourcompany.krs.models

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:krs.db", driver = "org.sqlite.JDBC")
    }
}
