package me.mgaldieri.pismostore.models

import me.mgaldieri.pismostore.toHex
import java.security.MessageDigest

data class User(val id: Int, val name: String, val email: String, val password: String) {
    fun checkPassword(password2Check: String) : Boolean {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedPassword = md.digest(password2Check.toByteArray()).toHex()

        return password.toLowerCase() == hashedPassword.toLowerCase()
    }
}