package me.mgaldieri.pismostore.dao

import me.mgaldieri.pismostore.DBHelper
import me.mgaldieri.pismostore.models.Session
import me.mgaldieri.pismostore.models.User
import org.sql2o.Connection

object SessionDAO {
    fun getSessionTokenByUserId(userId: Int) : String? {
        val query = "SELECT session_id, user_id FROM Session WHERE user_id = :userId"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return null
        val session: Session = conn.createQuery(query)
                .addParameter("userId", userId)
                .addColumnMapping("session_id", "sessionId")
                .addColumnMapping("user_id", "userId")
                .executeAndFetchFirst(Session::class.java) ?: return null

        return session.sessionId
    }

    fun getUserBySessionToken(token: String) : User? {
        val query = "SELECT u.id, u.name, u.email, u.password FROM Session AS s JOIN User as u ON s.user_id=u.id WHERE s.session_id = :token"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return null
        val user: User = conn.createQuery(query)
                .addParameter("token", token)
                .executeAndFetchFirst(User::class.java) ?: return null

        return user
    }

    fun getSessionByToken(token: String) : Session? {
        val query = "SELECT session_id, user_id FROM Session WHERE session_id = :token"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return null
        val session: Session = conn.createQuery(query)
                .addParameter("token", token)
                .addColumnMapping("session_id", "sessionId")
                .addColumnMapping("user_id", "userId")
                .executeAndFetchFirst(Session::class.java) ?: return null

        return session
    }

    fun deleteSessionByUserId(userId: Int) : Boolean {
        val query = "DELETE FROM Session WHERE user_id = :userId"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return false
        conn.createQuery(query)
                .addParameter("userId", userId)
                .addColumnMapping("user_id", "userId")
                .executeUpdate()

        return true
    }
}