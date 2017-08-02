package me.mgaldieri.pismostore.models

data class ErrorMessage(var type: String, var errorCode: String, var httpCode: Int, var message: String)