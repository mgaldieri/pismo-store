package me.mgaldieri.pismostore.models

data class Cart(var products: HashMap<Int, CartItem>, var totalPriceCents: Int?)