package com.dawit.androidapp4.data.repository

import com.dawit.androidapp4.data.local.SubscriptionStore
import com.dawit.androidapp4.data.model.Episode
import com.dawit.androidapp4.data.model.Podcast
import com.dawit.androidapp4.data.remote.ITunesApi
import com.dawit.androidapp4.data.remote.RssEpisodeParser
import com.dawit.androidapp4.domain.SearchFilterEngine
import com.dawit.androidapp4.domain.SearchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coordinates all podcast-related data operations.
 *
 * Primary constructor parameters:
 *
 * api:
 * Sends podcast searches and downloads RSS feeds.
 *
 * subscriptionStore:
 * Saves and retrieves subscriptions on the device.
 *
 * rssEpisodeParser:
 * Converts downloaded RSS XML into Episode objects.
 *
 * RssEpisodeParser has a default value, so callers do not have
 * to provide one unless they want to replace it for testing.
 */
class PodcastRepository(
    private val api: ITunesApi,
    private val subscriptionStore: SubscriptionStore,
    private val rssEpisodeParser: RssEpisodeParser = RssEpisodeParser()
) {

    /**
     * Searches Apple and then applies the selected advanced filter.
     */
    suspend fun search(
        query: String,
        mode: SearchMode,
        criteria: String
    ): List<Podcast> {

        // Send the general search request to Apple.
        val response = api.searchPodcasts(
            term = query
        )

        // Get the podcast list from Apple's response.
        val podcasts = response.results

        // Apply regex, word-count, episode-count, or date filtering.
        return SearchFilterEngine.apply(
            podcasts = podcasts,
            mode = mode,
            criteria = criteria
        )
    }

    /**
     * Retrieves all podcasts subscribed to by the user.
     *
     * This function is not suspend because SharedPreferences
     * provides the small amount of saved data immediately.
     */
    fun getSubscriptions(): List<Podcast> {
        return subscriptionStore.getAll()
    }

    /**
     * Checks whether a particular podcast is subscribed.
     */
    fun isSubscribed(trackId: Long): Boolean {
        return subscriptionStore.isSubscribed(trackId)
    }

    /**
     * Adds or removes a subscription.
     *
     * Returns true when subscribed and false when unsubscribed.
     */
    fun toggleSubscription(podcast: Podcast): Boolean {
        return subscriptionStore.toggle(podcast)
    }

    /**
     * Downloads and parses the episodes from a podcast RSS feed.
     */
    suspend fun getEpisodes(
        feedUrl: String
    ): List<Episode> {

        /**
         * Dispatchers.IO moves file and network processing away
         * from the main UI thread.
         */
        return withContext(Dispatchers.IO) {

            // Download the raw RSS response.
            val responseBody = api.downloadFeed(feedUrl)

            /**
             * "use" automatically closes ResponseBody and InputStream
             * when parsing finishes, including when an error occurs.
             */
            responseBody.use { body ->
                body.byteStream().use { inputStream ->
                    rssEpisodeParser.parse(inputStream)
                }
            }
        }
    }
}