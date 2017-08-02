package me.mgaldieri.pismostore.dao

import me.mgaldieri.pismostore.DBHelper
import me.mgaldieri.pismostore.buildUserCart
import me.mgaldieri.pismostore.models.Cart
import me.mgaldieri.pismostore.models.CartItem
import me.mgaldieri.pismostore.models.DBResult
import me.mgaldieri.pismostore.models.User
import org.sql2o.Connection

object CartDAO {
    fun userCartHasProductId(user: User, productId: Int) : Boolean {
        var query = "SELECT COUNT(*) FROM Cart WHERE user_id = :userId AND product_id = :productId"
//        val db = DBHelper.getInstance()
//        val conn: Connection = db.open() ?: return false
        val conn: Connection = DBHelper.getConnection()
        val itemCount: Int = conn.createQuery(query)
                .addParameter("userId", user.id)
                .addParameter("productId", productId)
                .executeScalar(Int::class.java)

        return itemCount > 0
    }

    fun getProductQtyInUserCart(user: User, productId: Int) : Int {
        val query = "SELECT SUM(qty) FROM Cart WHERE  user_id = :userId AND product_id = :productId"
//        val db = DBHelper.getInstance()
//        val conn: Connection = db.open() ?: return false
        val conn: Connection = DBHelper.getConnection()
        val itemSum: Int = conn.createQuery(query)
                .addParameter("userId", user.id)
                .addParameter("productId", productId)
                .executeScalar(Int::class.java)

        return itemSum
    }

    fun getCartByUser(user: User) : DBResult {
        val result = DBResult(null, false)

        val query = "SELECT product_id, qty FROM Cart WHERE user_id = :userId"
//        val db = DBHelper.getInstance()
//        val conn: Connection = db.open() ?: return false
        val conn: Connection = DBHelper.getConnection()
        val items: List<CartItem> = conn.createQuery(query)
                .addColumnMapping("product_id", "productId")
                .addParameter("userId", user.id)
                .executeAndFetch(CartItem::class.java) ?: return result

        val products = HashMap<Int, CartItem>()
        for (item in items) {
            products.put(item.productId, item)
        }
        val cart = buildUserCart(products) ?: return result

        result.data = cart
        result.successful = true

        return result
    }

    fun addProductIdToUserCart(productId: Int, qty: Int, user: User) : DBResult {
        val result = DBResult(null, false)

        val query = "INSERT INTO Cart (user_id, product_id, qty) VALUES (:userId, :productId, :qty)"
//        val db = DBHelper.getInstance()
//        val conn: Connection = db.open() ?: return false
        val conn: Connection = DBHelper.getConnection()
        conn.createQuery(query)
                .addParameter("userId", user.id)
                .addParameter("productId", productId)
                .addParameter("qty", qty)
                .executeUpdate()

        return getCartByUser(user)
    }

    fun removeProductIdFromUserCart(productId: Int, user: User) : DBResult {
        val result = DBResult(null, false)

        val query = "DELETE FROM Cart WHERE user_id = :userId AND product_id = :productId"
//        val db = DBHelper.getInstance()
//        val conn: Connection = db.open() ?: return false
        val conn: Connection = DBHelper.getConnection()
        conn.createQuery(query)
                .addParameter("userId", user.id)
                .addParameter("productId", productId)
                .executeUpdate()

        return getCartByUser(user)
    }

    fun setProductQtyInUserCart(productId: Int, qty: Int, user: User) : DBResult {
        val result = DBResult(null, false)

        val query = "UPDATE Cart set qty = :qty WHERE user_id = :userId AND product_id = :productId"
//        val db = DBHelper.getInstance()
//        val conn: Connection = db.open() ?: return false
        val conn: Connection = DBHelper.getConnection()
        conn.createQuery(query)
                .addParameter("qty", qty)
                .addParameter("userId", user.id)
                .addParameter("productId", productId)
                .executeUpdate()

        return getCartByUser(user)
    }

    fun emptyUserCart(user: User) : DBResult {
        val result = DBResult(null, false)

        val query = "DELETE FROM Cart WHERE user_id = :userId"
        val conn: Connection = DBHelper.getConnection()
        conn.createQuery(query)
                .addParameter("userId", user.id)
                .executeUpdate()

        return getCartByUser(user)
    }
}