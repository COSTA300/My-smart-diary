package com.example.ai

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val responseText: String) : GeminiResult()
    data class Error(val exceptionMessage: String) : GeminiResult()
    object KeyNotConfigured : GeminiResult()
}

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
        history: List<com.example.data.DecryptedChatMessage>,
        entryText: String? = null,
        apiKeyOverride: String = ""
    ): GeminiResult = withContext(Dispatchers.IO) {
        // Use the manual key if supplied (e.g. from app settings), otherwise fallback to the BuildConfig key
        val apiKey = apiKeyOverride.trim().ifBlank { 
            com.example.BuildConfig.GEMINI_API_KEY.trim() 
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "placeholder") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local/offline engine.")
            return@withContext GeminiResult.KeyNotConfigured
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

        val contentsList = mutableListOf<GeminiContent>()

        // 1. If we have the initial entry description, prepend it as the first 'user' interaction 
        // to provide full emotional journal context to the live model!
        if (!entryText.isNullOrBlank()) {
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "The user has logged their mood today as: $mood. Here is their reflection/message: $entryText"))
                )
            )
        }

        // 2. Iterate and append existing chat history
        // To prevent network packet bloat and stay within rate units, limit to the last 10 messages
        val recentHistory = history.takeLast(10)
        recentHistory.forEach { msg ->
            val role = if (msg.sender == "user") "user" else "model"
            val lastAdded = contentsList.lastOrNull()
            
            if (lastAdded != null && lastAdded.role == role) {
                // If consecutive roles are identical, merge their text parts to satisfy API alternating design
                val mergedParts = lastAdded.parts + GeminiPart(text = msg.text)
                contentsList[contentsList.size - 1] = lastAdded.copy(parts = mergedParts)
            } else {
                contentsList.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
        }

        // 3. Add the active input, taking care of repetition and alternating structures
        val lastInputMsg = recentHistory.lastOrNull()
        if (lastInputMsg == null || lastInputMsg.text != userInput) {
            val role = "user"
            val lastAdded = contentsList.lastOrNull()
            if (lastAdded != null && lastAdded.role == role) {
                // Merge parts
                val mergedParts = lastAdded.parts + GeminiPart(text = userInput)
                contentsList[contentsList.size - 1] = lastAdded.copy(parts = mergedParts)
            } else {
                contentsList.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = userInput))
                    )
                )
            }
        }

        // 4. Critical Correction: A chat thread MUST always start with a 'user' turn in Gemini
        if (contentsList.isNotEmpty() && contentsList.first().role == "model") {
            contentsList.add(0, GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = "[Initializing conversation thread with the emotional companion]"))
            ))
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
                Log.e(TAG, "Gemini API response content was empty/blank.")
                GeminiResult.Error("Empty response candidate returned by model.")
            } else {
                GeminiResult.Success(responseText)
            }
        } catch (e: HttpException) {
            val msg = "HTTP ${e.code()}: ${e.message()}"
            Log.e(TAG, msg, e)
            GeminiResult.Error(msg)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Uncaught Network IO Exception"
            Log.e(TAG, "Exception during Gemini REST invocation: $msg", e)
            GeminiResult.Error(msg)
        }
    }
}
