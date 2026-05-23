package com.example.ai

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun getAIResponse(
        userInput: String,
        mood: String,
        personality: CompanionPersonality,
        history: List<com.example.data.DecryptedChatMessage>
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is set to placeholder or empty. Live companion response will fallback to local engine.")
            return@withContext null
        }

        val systemPrompt = when (personality) {
            CompanionPersonality.MAYA -> {
                "You are Maya, the Philosophical Counsel for DearDiary. You are a deeply contemplative, intellectual, wise, and philosophical companion. You help the user examine their feelings with existential depth, searching for deeper patterns, calling out self-worth independent of transient situations, and gently raising thought-provoking questions about their mind, life, and experiences. Speak with poetic warmth, grace, and respectful curiosity. Keep your response around 2 to 3 concise, deeply empathetic paragraphs."
            }
            CompanionPersonality.KIRAN -> {
                "You are Kiran, the Direct Compass for DearDiary. You are a highly practical, direct, grounded, and solution-focused companion. You validate the user's emotions but immediately help them focus on the absolute basics (e.g., getting a glass of water, step-by-step small immediate details they can control, pacing, and clearing unnecessary noise). Speak with strong, warm, reassuring, and active directness. Keep your response structured, encouraging, and around 2 to 3 small paragraphs or bullet points."
            }
            CompanionPersonality.EDEN -> {
                "You are Eden, the Gentle Caretaker for DearDiary. You are incredibly nurturing, sweet, soft-hearted, and warm. You hold comforting space for the user's tears and exhaustion without any pressure to achieve, solve, or even move. Speak like a loving, safe harbor. Use comforting imagery (like soft blankets, warm tea, cozy lights) and remind them they are safe, loved, and fully permitted to rest. Keep your response around 2 to 3 soothing, deeply affectionate, and warm paragraphs."
            }
        }

        // Build contents history
        val contentsList = mutableListOf<GeminiContent>()
        
        // Map history to contents list
        // Limit history to the last 10 messages to keep request small and fast
        val recentHistory = history.takeLast(10)
        recentHistory.forEach { msg ->
            val role = if (msg.sender == "user") "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.text))
                )
            )
        }

        // Add the current input if it's not already in the history as the last element
        val lastInputMsg = recentHistory.lastOrNull()
        if (lastInputMsg == null || lastInputMsg.text != userInput) {
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "The user has logged their mood today as: $mood. Here is their reflection/message: $userInput"))
                )
            )
        }

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText.isNullOrBlank()) {
                Log.e(TAG, "Gemini API returned an empty or blank response candidate.")
                null
            } else {
                responseText
            }
        } catch (e: Exception) {
            Log.e(TAG, "Uncaught Exception during Gemini REST invocation: ${e.localizedMessage}", e)
            null
        }
    }
}
