package com.dawit.androidapp4.data.local

import android.content.Context
import com.dawit.androidapp4.data.model.Podcast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Saves, retrieves, and removes podcast subscriptions.
 *
 * Primary constructor:
 * The class requires an Android Context so it can access SharedPreferences.
 *
 * Example:
 * val store = SubscriptionStore(applicationContext)
 */
class SubscriptionStore(
    context: Context
) {
    /**
     * SharedPreferences stores small amounts of application data.
     *
     * MODE_PRIVATE means only SuperPodcast can access this file.
     */
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Gson converts Kotlin objects into JSON and JSON back into objects.
     */
    private val gson = Gson()

    /**
     * Gson needs this TypeToken to understand that the saved JSON
     * represents a List of Podcast objects.
     */
    private val podcastListType =
        object : TypeToken<List<Podcast>>() {}.type

    /**
     * Retrieves all saved podcast subscriptions.
     */
    fun getAll(): List<Podcast> {

        // Read the saved JSON text.
        val json = preferences.getString(
            KEY_SUBSCRIPTIONS,
            null
        ) ?: return emptyList()

        /**
         * Attempt to convert the JSON into a List<Podcast>.
         *
         * If the data is missing or damaged, return an empty list
         * instead of crashing the application.
         */
        return try {
            gson.fromJson<List<Podcast>>(
                json,
                podcastListType
            ) ?: emptyList()
        } catch (error: Exception) {
            emptyList()
        }
    }

    /**
     * Checks whether a podcast is already subscribed.
     */
    fun isSubscribed(trackId: Long): Boolean {
        return getAll().any { podcast ->
            podcast.trackId == trackId
        }
    }

    /**
     * Adds a podcast when it is not subscribed.
     *
     * Removes the podcast when it is already subscribed.
     *
     * Return value:
     * true  = the podcast is now subscribed
     * false = the podcast is now unsubscribed
     */
    fun toggle(podcast: Podcast): Boolean {

        // Create a mutable copy because getAll() returns a read-only list.
        val podcasts = getAll().toMutableList()

        // Search for a podcast with the same Apple track ID.
        val existingIndex = podcasts.indexOfFirst { savedPodcast ->
            savedPodcast.trackId == podcast.trackId
        }

        val isNowSubscribed: Boolean

        if (existingIndex >= 0) {
            // The podcast already exists, so remove it.
            podcasts.removeAt(existingIndex)
            isNowSubscribed = false
        } else {
            // The podcast is not saved, so add it.
            podcasts.add(podcast)
            isNowSubscribed = true
        }

        // Convert the updated list into JSON.
        val updatedJson = gson.toJson(podcasts)

        // Save the JSON inside SharedPreferences.
        preferences
            .edit()
            .putString(KEY_SUBSCRIPTIONS, updatedJson)
            .apply()

        return isNowSubscribed
    }

    /**
     * Constants used only by SubscriptionStore.
     */
    private companion object {

        // Name of the SharedPreferences file.
        const val PREFERENCES_NAME =
            "super_podcast_preferences"

        // Key used to locate the subscription JSON.
        const val KEY_SUBSCRIPTIONS =
            "subscriptions"
    }
}