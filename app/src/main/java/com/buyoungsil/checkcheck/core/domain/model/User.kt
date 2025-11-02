package com.buyoungsil.checkcheck.core.domain.model

data class User(
    val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)