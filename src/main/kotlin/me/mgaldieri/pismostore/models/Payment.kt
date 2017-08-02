package me.mgaldieri.pismostore.models

data class Payment(val id: Long?, val userId: Int, val token: String, val priceCents: Int)