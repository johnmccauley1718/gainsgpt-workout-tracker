package com.john.gainsgpt.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>
) {
    @Serializable
    data class Choice(val message: ChatMessage)
}

object ChatGPTService {
    private const val API_URL = "https://openaiproxy-d4iznw4ksq-uc.a.run.app"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun buildPlan(prompt: String): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val reqBody = ChatCompletionRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(ChatMessage("user", prompt))
        )
        val mediaType = "application/json".toMediaType()
        val jsonBody = json.encodeToString(reqBody)
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { resp ->
                val body = resp.body?.string()
                println("DEBUG: Response body = $body")

                if (resp.isSuccessful && body != null) {
                    val result = json.decodeFromString<ChatCompletionResponse>(body)
                    result.choices.firstOrNull()?.message?.content?.trim()
                } else {
                    println("DEBUG: Request failed. Code=${resp.code}, Body=$body")
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("DEBUG: Exception: ${e.localizedMessage}")
            null
        }
    }
}
