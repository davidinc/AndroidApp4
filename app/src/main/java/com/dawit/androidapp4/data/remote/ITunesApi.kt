package com.dawit.androidapp4.data.remote

import com.dawit.androidapp4.data.model.PodcastResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Defines the requests sent to Apple and podcast RSS servers.
 */
interface ITunesApi {
    /**
     * Searches Apple for podcasts matching the supplied term.
     */
    @GET("search")
    suspend fun searchPodcasts(
        @Query("term") term: String,
        @Query("media") media: String = "podcast",
        @Query("entity") entity: String = "podcast",
        @Query("country") country: String = "CA",
        @Query("limit") limit: Int = 100
    ): PodcastResponse

    /**
     * Downloads a podcast's RSS feed.
     *
     * @Url is used because every podcast has a different complete feed address.
     */
    @GET
    suspend fun downloadFeed(
        @Url feedUrl: String
    ): ResponseBody
}