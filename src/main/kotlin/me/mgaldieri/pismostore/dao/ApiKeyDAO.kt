package me.mgaldieri.pismostore.dao

import me.mgaldieri.pismostore.DBHelper
import org.sql2o.Connection

object ApiKeyDAO {
    fun getApiKey() : String? {
        val query = "SELECT token FROM ApiKey WHERE id = 1"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return null
        val key: String = conn.createQuery(query)
                .executeAndFetchFirst(String::class.java) ?: return null

        return key
    }
}