package me.mgaldieri.pismostore

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.mgaldieri.pismostore.models.ErrorMessage
import me.mgaldieri.pismostore.models.ResponseData
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.IOException

enum class HttpVerb {
    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
}

val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8")

object HTTPHelper {
    private val client: OkHttpClient = OkHttpClient()

    fun getClient() : OkHttpClient {
        return client
    }

    fun doRequest(url: String, method: HttpVerb, payload: JsonNode?) : ResponseData {
        var data: ResponseData
        try {
            val builder = okhttp3.Request.Builder()
                    .header("Authorization", API_KEY)
                    .url(url)

            val content = if (payload != null) {
                payload.toString()
            } else {
                ""
            }

            when (method) {
                HttpVerb.GET -> {
                    builder.get()
                }
                HttpVerb.PUT -> {
                    builder.put(RequestBody.create(MEDIA_TYPE_JSON, content))
                }
                HttpVerb.POST -> {
                    builder.post(RequestBody.create(MEDIA_TYPE_JSON, content))
                }
                HttpVerb.DELETE -> {
                    builder.delete(RequestBody.create(MEDIA_TYPE_JSON, content))
                }
                HttpVerb.HEAD -> {
                    builder.head()
                }
            }

            val req = builder.build()
            val res = HTTPHelper.getClient().newCall(req).execute()

            val mapper = jacksonObjectMapper()
            data = mapper.readValue(res.body()!!.bytes(), ResponseData::class.java)
        } catch (e: IOException) {
            val err = ErrorMessage("Server Error", "0000", 502, "Bad Gateway")
            data = ResponseData(null, err)
        }
        return data
    }
}