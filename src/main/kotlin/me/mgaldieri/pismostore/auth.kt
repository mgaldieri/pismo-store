package me.mgaldieri.pismostore

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import me.mgaldieri.pismostore.dao.SessionDAO
import me.mgaldieri.pismostore.models.Session
import me.mgaldieri.pismostore.models.User
import spark.Request
import spark.Response
import spark.Spark
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex() : String{
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}

fun generateSimpleHash() : String {
    val random = BigInteger(128, SecureRandom()).toByteArray()
    val md = MessageDigest.getInstance("SHA-1")
    return md.digest(random).toHex()
}

fun trySessionToken() : String {
    val _t = generateSimpleHash()
    return if (SessionDAO.getSessionByToken(_t) == null) { _t } else { trySessionToken() }
}

fun loginUser(user: User) : String? {
    var token = SessionDAO.getSessionTokenByUserId(user.id)
    if (token == null) {
        token = trySessionToken()
        val session = Session(token, user.id)
        if (!session.save()) return null
    }

    return Jwts.builder()
            .setIssuer("pismostore")
            .setSubject(token)
            .signWith(SignatureAlgorithm.HS512, API_SECRET.toByteArray())
            .compact()
}

fun logoutUser(user: User) : Boolean {
    return SessionDAO.deleteSessionByUserId(user.id)
}

fun getJwtString(request: Request) : String? {
    val header = request.headers("Authorization") ?: return null
    return header.removePrefix("Bearer ")
}

fun getCurrentUser(request: Request) : User? {
    val jwtString = getJwtString(request) ?: return null
    try {
        val sessionToken = Jwts.parser().setSigningKey(API_SECRET.toByteArray()).parseClaimsJws(jwtString).body.subject ?: return null
        return SessionDAO.getUserBySessionToken(sessionToken)
    } catch (ignored: Exception) {
        return null
    }
}

fun checkAuthorization(request: Request, response: Response) {
    getCurrentUser(request) ?: Spark.halt(401, abort(response, "Unauthorized", 401))
}