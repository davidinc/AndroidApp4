package com.dawit.androidapp4.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dawit.androidapp4.data.local.SubscriptionStore
import com.dawit.androidapp4.data.remote.RetrofitClient
import com.dawit.androidapp4.data.repository.PodcastRepository

/**
 * Constructs PodcastViewModel with all its required dependencies.
 *
 * Primary constructor:
 * The factory receives an Android Context so it can create
 * SubscriptionStore.
 */
class PodcastViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    /**
     * applicationContext belongs to the complete application.
     *
     * We store applicationContext instead of an Activity Context
     * to prevent the factory from accidentally keeping an old
     * Activity in memory.
     */
    private val applicationContext =
        context.applicationContext

    /**
     * Android calls this function when MainActivity requests
     * a PodcastViewModel.
     *
     * T represents the type of ViewModel Android is requesting.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        /**
         * Confirm that Android requested the correct ViewModel type.
         *
         * If the wrong ViewModel is requested, require() stops
         * execution with a clear error.
         */
        require(
            modelClass.isAssignableFrom(
                PodcastViewModel::class.java
            )
        ) {
            "Unknown ViewModel class: ${modelClass.name}"
        }

        /**
         * Construct the local subscription data source.
         */
        val subscriptionStore = SubscriptionStore(
            context = applicationContext
        )

        /**
         * Construct the repository.
         *
         * RetrofitClient.api supplies the network service.
         * RssEpisodeParser uses its default constructor value.
         */
        val repository = PodcastRepository(
            api = RetrofitClient.api,
            subscriptionStore = subscriptionStore
        )

        /**
         * Construct and return PodcastViewModel.
         *
         * The cast is safe because we checked modelClass above.
         */
        return PodcastViewModel(
            repository = repository
        ) as T
    }
}