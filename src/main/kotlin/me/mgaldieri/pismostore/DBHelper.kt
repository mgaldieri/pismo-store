package me.mgaldieri.pismostore

import org.sql2o.Connection
import org.sql2o.Sql2o

object DBHelper {
    private val db: Sql2o = Sql2o("jdbc:h2:mem:store", null, null)
    var conn: Connection? = null

    fun getInstance() : Sql2o {
        return db
    }

    fun getConnection() : Connection {
        if (conn == null) {
            conn = db.open()
        }
        return conn!!
    }
}