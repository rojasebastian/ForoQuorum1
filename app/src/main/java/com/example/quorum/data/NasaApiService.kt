package com.example.quorum.data
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApiService {
    @GET("planetary/apod")
    suspend fun getAstronomyPictureOfTheDay(@Query("api_key") apiKey: String): Apod
}

// Objeto Singleton para acceder a la API
object RetrofitClient {
    private const val BASE_URL = "https://api.nasa.gov/"
    val instance: NasaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NasaApiService::class.java)
    }
}
