package me.mgaldieri.pismostore

import me.mgaldieri.pismostore.controllers.StoreController
import me.mgaldieri.pismostore.controllers.UserController
import me.mgaldieri.pismostore.dao.ApiKeyDAO
import spark.Spark.*

val WAREHOUSE_PORT = System.getenv("WAREHOUSE_SERVER_PORT") ?: 8000
val WAREHOUSE_IP = System.getenv("WAREHOUSE_SERVER_IP") ?: "127.0.0.1"
val WAREHOUSE_URL = "http://$WAREHOUSE_IP:$WAREHOUSE_PORT"
var API_KEY: String? = null

fun main(args: Array<String>) {
    val port = 8001

    port(port)
    initDB()

    if (API_KEY == null) API_KEY = ApiKeyDAO.getApiKey()

    println("Servidor Warehouse iniciado em 127.0.0.1:$port")

    // Ping endpoint
    get("/ping") { req, res ->
        "Alive"
    }

    get("/products") { req, res -> StoreController.getProducts(req, res) }

    get("/product/:productId") { req, res -> StoreController.getProduct(req, res) }

    path("/user") {
        post("/login") { req, res -> UserController.login(req, res) }

        post("/logout") { req, res -> UserController.logout(req, res) }

        post("/checkout") { req, res -> UserController.checkout(req, res) }

        path("/cart") {
            get("") { req, res -> UserController.getShoppingCart(req, res) }

            put("/:productId") { req, res -> UserController.addProductToShoppingCart(req, res) }

            delete("/:productId") { req, res -> UserController.removeProductFromShoppingCart(req, res) }

            post("/:productId/increase") { req, res -> UserController.increaseProductInShoppingCart(req, res) }

            post("/:productId/decrease") { req, res -> UserController.decreaseProductInShoppingCart(req, res) }
        }
    }
}