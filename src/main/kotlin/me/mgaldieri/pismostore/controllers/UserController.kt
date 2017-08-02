package me.mgaldieri.pismostore.controllers

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.mgaldieri.pismostore.*
import me.mgaldieri.pismostore.dao.CartDAO
import me.mgaldieri.pismostore.dao.UserDAO
import me.mgaldieri.pismostore.models.Cart
import me.mgaldieri.pismostore.models.User
import spark.Request
import spark.Response
import spark.Spark

object UserController {
    fun login(request: Request, response: Response) : String {
        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val email = jsonBody.get("email")
            val password = jsonBody.get("password")

            if (email.isNull || password.isNull) {
                Spark.halt(403, abort(response, "Invalid credentials", 403))
            }

            val (data, successful) = UserDAO.getUserByEmail(email.asText())
            if (!successful) {
                Spark.halt(500, abort(response, "Error retrieving user", 500))
            }
            if (data == null) {
                Spark.halt(404, abort(response, "User not found", 404))
            }

            val user = data as User
            if (!user.checkPassword(password.asText())) {
                Spark.halt(402, abort(response, "Invalid password", 402))
            }

            val jwt = loginUser(user) ?: Spark.halt(500, abort(response, "Error logging user in", 500))

            return success(response, mapOf(
                    "jwt" to jwt
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }

    fun logout(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))
        if (!logoutUser(user!!)) Spark.halt(401, abort(response, "Error logging user out", 401))

        return success(response, null)
    }

    fun checkout(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))

        // Get user cart
        val (data, successful) = CartDAO.getCartByUser(user!!)
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving shopping cart", 500))
        }

        val cart = data as Cart
        if (cart.products.size == 0) {
            Spark.halt(406, abort(response, "Cart empty", 406))
        }

        val error = chargeUserForCart(user, cart)
        if (error != null) {
            Spark.halt(error.httpCode, abort(response, error.message, error.httpCode))
        }

        return success(response, null)
    }

    fun getShoppingCart(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))
//        val user = User(1, "TESTE", "TESTE@TESTE.COM", "TESTE123")

        val (data, successful) = CartDAO.getCartByUser(user!!)
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving shopping cart", 500))
        }

        return success(response, mapOf(
                "cart" to data
        ))
    }

    fun addProductToShoppingCart(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))

        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val qty = jsonBody.get("qty")

            if (qty.isNull) {
                Spark.halt(403, abort(response, "Missing product quantity in request body", 403))
            }

            // Get product from warehouse
            val productId = request.params("productId") ?: Spark.halt(400, abort(response, "Missing product id", 400))
            val url = "$WAREHOUSE_URL/vendor/product/$productId"
            val whResp = HTTPHelper.doRequest(url, HttpVerb.GET, null)
            if (whResp.error != null) {
                val err = whResp.error
                Spark.halt(err.httpCode, abort(response, err.message, err.httpCode))
            }

            val whData = whResp.data as LinkedHashMap<*, *>
            val whProduct = whData.get("product") as LinkedHashMap<*, *>
            val whProductId = whProduct.get("id") as Int

            // Check stock availability
            val stockQty = whProduct.get("qty") as Int
            if (qty.asInt() > stockQty) {
                Spark.halt(406, abort(response, "Not enough items in stock", 406))
            }

            // Check if product is already in shopping cart
            if (CartDAO.userCartHasProductId(user!!, whProductId)) {
                Spark.halt(406, abort(response, "Item already in shopping cart", 406))
            }

            // Add product to shopping cart
            val (data, successful) = CartDAO.addProductIdToUserCart(whProductId, qty.asInt(), user!!)
            if (!successful) {
                Spark.halt(500, abort(response, "Error adding item to shopping cart", 500))
            }

            return success(response, mapOf(
                    "cart" to data
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }

    fun removeProductFromShoppingCart(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))

        // Get product from warehouse
        val productId = request.params("productId") ?: Spark.halt(400, abort(response, "Missing product id", 400))
        val url = "$WAREHOUSE_URL/vendor/product/$productId"
        val whResp = HTTPHelper.doRequest(url, HttpVerb.GET, null)
        if (whResp.error != null) {
            val err = whResp.error
            Spark.halt(err.httpCode, abort(response, err.message, err.httpCode))
        }

        val whData = whResp.data as LinkedHashMap<*, *>
        val whProduct = whData.get("product") as LinkedHashMap<*, *>
        val whProductId = whProduct.get("id") as Int

        // Check if product is not in shopping cart
        if (!CartDAO.userCartHasProductId(user!!, whProductId)) {
            Spark.halt(406, abort(response, "Item not in shopping cart", 406))
        }

        // Remove item from shopping cart
        val (data, successful) = CartDAO.removeProductIdFromUserCart(whProductId, user)
        if (!successful) {
            Spark.halt(500, abort(response, "Error removing item from shopping cart", 500))
        }

        return success(response, mapOf(
                "cart" to data
        ))
    }

    fun increaseProductInShoppingCart(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))

        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val qty = jsonBody.get("qty")

            if (qty.isNull) {
                Spark.halt(403, abort(response, "Missing product quantity in request body", 403))
            }

            // Get product from warehouse
            val productId = request.params("productId") ?: Spark.halt(400, abort(response, "Missing product id", 400))
            val url = "$WAREHOUSE_URL/vendor/product/$productId"
            val whResp = HTTPHelper.doRequest(url, HttpVerb.GET, null)
            if (whResp.error != null) {
                val err = whResp.error
                Spark.halt(err.httpCode, abort(response, err.message, err.httpCode))
            }

            val whData = whResp.data as LinkedHashMap<*, *>
            val whProduct = whData.get("product") as LinkedHashMap<*, *>
            val whProductId = whProduct.get("id") as Int

            // Check if product is not in shopping cart
            if (!CartDAO.userCartHasProductId(user!!, whProductId)) {
                Spark.halt(406, abort(response, "Item not in shopping cart", 406))
            }

            // Check stock availability
            val stockQty = whProduct.get("qty") as Int
            val cartQty = CartDAO.getProductQtyInUserCart(user, whProductId)
            val totalQty = qty.asInt() + cartQty

            if (totalQty > stockQty) {
                Spark.halt(406, abort(response, "Not enough items in stock", 406))
            }

            // Increase product quantity in shopping cart
            val (data, successful) = CartDAO.setProductQtyInUserCart(whProductId, totalQty, user)
            if (!successful) {
                Spark.halt(500, abort(response, "Error increasing item quantity in shopping cart", 500))
            }

            return success(response, mapOf(
                    "cart" to data
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }

    fun decreaseProductInShoppingCart(request: Request, response: Response) : String {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))

        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val qty = jsonBody.get("qty")

            if (qty.isNull) {
                Spark.halt(403, abort(response, "Missing product quantity in request body", 403))
            }

            // Get product from warehouse
            val productId = request.params("productId") ?: Spark.halt(400, abort(response, "Missing product id", 400))
            val url = "$WAREHOUSE_URL/vendor/product/$productId"
            val whResp = HTTPHelper.doRequest(url, HttpVerb.GET, null)
            if (whResp.error != null) {
                val err = whResp.error
                Spark.halt(err.httpCode, abort(response, err.message, err.httpCode))
            }

            val whData = whResp.data as LinkedHashMap<*, *>
            val whProduct = whData.get("product") as LinkedHashMap<*, *>
            val whProductId = whProduct.get("id") as Int

            // Check if product is not in shopping cart
            if (!CartDAO.userCartHasProductId(user!!, whProductId)) {
                Spark.halt(406, abort(response, "Item not in shopping cart", 406))
            }

            // Check for zero quantity
            val cartQty = CartDAO.getProductQtyInUserCart(user, whProductId)
            var totalQty = cartQty - qty.asInt()
            if (totalQty < 0) totalQty = 0

            val data: Cart
            val successful: Boolean
            // If final qty is zero, delete records from db
            if (totalQty == 0) {
                val (d, s) = CartDAO.removeProductIdFromUserCart(whProductId, user)
                data = d as Cart
                successful = s
            } else {
                // Decrease product quantity in shopping cart
                val (d, s) = CartDAO.setProductQtyInUserCart(whProductId, totalQty, user)
                data = d as Cart
                successful = s
            }
            if (!successful) {
                Spark.halt(500, abort(response, "Error decreasing item quantity in shopping cart", 500))
            }

            return success(response, mapOf(
                    "cart" to data
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }

}