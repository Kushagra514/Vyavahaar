package org.vyavahaar.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 10.0.2.2 points to host machine from Android Emulator; 127.0.0.1 for local testing
    // Override for physical device: -Dvyavahaar.baseUrl=http://<your-computer-ip>:8000/
    var baseUrl: String = System.getProperty("vyavahaar.baseUrl", "http://10.164.19.32:8000/")

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: VyavahaarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VyavahaarApiService::class.java)
    }
}
