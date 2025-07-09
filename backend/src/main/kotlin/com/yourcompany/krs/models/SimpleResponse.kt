package com.yourcompany.krs.models

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse(
    val success: Boolean,
    val message: String? = null,
    val nim: String? = null,
    val role: String? = null,
    val username: String? = null,
    val id: Int? = null
)
