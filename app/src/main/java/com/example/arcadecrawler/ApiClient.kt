package com.example.arcadecrawler

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
data class ColorResponse(val color: String)

data class SkinResponse(
    val mushrooms: List<String>,
    val guns: List<String>,
    val scorpions: List<String>
)
data class LeaderboardEntry(
    val id: Int,
    val name: String,
    val score: Int,
    val created_at: String
)
data class UpdateScoreRequest(
    val name: String,
    val password: String,
    val score: Int
)
data class UpdateScoreResponse(
    val success: Boolean,
    val message: String,
    val data: UpdateScoreData?
)
data class UpdateScoreData(
    val name: String,
    val score: Int,
    val rank: Int
)
data class MushroomLayoutResponse(
    val layout: List<List<Float>>
)
data class PowerUp(
    val id: Int,
    val name: String,
    val description: String,
    val duration: Long // in milliseconds
)
data class CentipedeDestroyedPayload(val centipedeDestroyed: Boolean = true)

interface CrawlerApi {

    @GET("color")
    suspend fun getCentipedeColor(): ColorResponse

    @GET("themePack/image")
    @Streaming
    suspend fun getThemeImage(): Response<ResponseBody>

    @GET("themePack/audio")
    @Streaming
    suspend fun getThemeAudio(): Response<ResponseBody>

    @GET("skins")
    suspend fun getSkins(): SkinResponse
    @GET("leaderboard")
    suspend fun getLeaderboard(): List<LeaderboardEntry>
    @POST("updatelead")
    suspend fun updateScore(@Body request: UpdateScoreRequest): UpdateScoreResponse
    @GET("mushroomLayout")
    suspend fun getMushroomLayout(): MushroomLayoutResponse
        @POST("powerUps")
    suspend fun getPowerUps(@Body payload: CentipedeDestroyedPayload): List<PowerUp>
}

object ApiClient {
    private const val BASE_URL = "https://crawler-connect.vercel.app/" // Replace with actual base URL

    val api: CrawlerApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CrawlerApi::class.java)
    }
}
