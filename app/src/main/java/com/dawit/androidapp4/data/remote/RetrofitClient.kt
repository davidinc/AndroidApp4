package com.dawit.androidapp4.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Creates and provides the Retrofit networking service.
 *
 * The "object" keyword creates a singleton. This means the application
 * uses one RetrofitClient instead of constructing multiple clients.
 */
object RetrofitClient {

    /**
     * This is the main address used by Apple's iTunes Search API.
     *
     * Retrofit requires the base URL to end with a forward slash.
     */
    private const val BASE_URL = "https://itunes.apple.com/"

    /**
     * OkHttpClient handles the low-level connection details.
     *
     * We configure it with:
     * 1. A custom DnsSelector to help with emulator resolution issues.
     * 2. A Logging Interceptor to see network traffic in Logcat.
     * 3. Increased timeouts for unstable connections.
     */
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .dns(DnsSelector())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the implementation of our ITunesApi interface.
     *
     * "by lazy" means Retrofit will not be created until the application
     * uses the api property for the first time.
     */
    val api: ITunesApi by lazy {

        // Begin constructing the Retrofit networking client.
        Retrofit.Builder()

            // Set Apple's main API address.
            .baseUrl(BASE_URL)

            // Use our configured OkHttpClient.
            .client(client)

            // Convert Apple's JSON response into Kotlin data classes.
            .addConverterFactory(GsonConverterFactory.create())

            // Finish constructing the Retrofit object.
            .build()

            // Create an implementation of the ITunesApi interface.
            .create(ITunesApi::class.java)
    }
}