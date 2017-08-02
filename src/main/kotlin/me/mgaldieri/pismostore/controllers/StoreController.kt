package me.mgaldieri.pismostore.controllers

import me.mgaldieri.pismostore.*
import spark.Request
import spark.Response
import spark.Spark

object StoreController {
    fun getProducts(request: Request, response: Response) : String {
        val url = "$WAREHOUSE_URL/vendor/products"

        val data = HTTPHelper.doRequest(url, HttpVerb.GET, null)
        if (data.error != null) {
            val err = data.error
            Spark.halt(err.httpCode, abort(response, err.message, err.httpCode))
        }

        return success(response, data.data)
    }

    fun getProduct(request: Request, response: Response) : String {
        val productId = request.params("productId") ?: Spark.halt(400, abort(response, "Missing product id", 400))
        val url = "$WAREHOUSE_URL/vendor/product/$productId"

        val data = HTTPHelper.doRequest(url, HttpVerb.GET, null)
        if (data.error != null) {
            val err = data.error
            Spark.halt(err.httpCode, abort(response, err.message, err.httpCode))
        }

        return success(response, data.data)
    }
}