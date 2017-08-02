package me.mgaldieri.pismostore.dao

import me.mgaldieri.pismostore.DBHelper
import me.mgaldieri.pismostore.models.DBResult
import me.mgaldieri.pismostore.models.Payment
import org.sql2o.Connection

object PaymentDAO {
    fun save(payment: Payment) : DBResult {
        val result = DBResult(null, false)

        val query = "INSERT INTO Payment (user_id, token, price_cents) values (:userId, :token, :priceCents)"
        val conn: Connection = DBHelper.getConnection()
        conn.createQuery(query, true)
                .addParameter("userId", payment.userId)
                .addParameter("token", payment.token)
                .addParameter("priceCents", payment.priceCents)
                .executeUpdate()

        result.successful = true
        return result
    }
}