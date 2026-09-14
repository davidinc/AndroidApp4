package com.dawit.androidapp4.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dawit.androidapp4.data.local.SubscriptionStore
import com.dawit.androidapp4.data.model.Podcast
import com.dawit.androidapp4.data.remote.RetrofitClient
import com.dawit.androidapp4.data.repository.PodcastRepository

/**
 * Constructs PodcastDetailViewModel and its dependencies.
 *
 * Primary constructor:
 *
 * context:
 * Required to create SubscriptionStore.
 *
 * podcast:
 * The podcast selected on the main screen.
 */
class PodcastDetailViewModelFactory(
    context: Context,
    private val podcast: Podcast
) : ViewModelProvider.Factory {

    /**
     * Store the application Context instead of the Activity Context
     * to prevent accidental Activity memory leaks.
     */
    private val applicationContext =
        context.applicationContext

    /**
     * Android calls this function when PodcastDetailActivity
     * requests its ViewModel.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        // Confirm that the requested type is PodcastDetailViewModel.
        require(
            modelClass.isAssignableFrom(
                PodcastDetailViewModel::class.java
            )
        ) {
            "Unknown ViewModel class: ${modelClass.name}"
        }

        // Create the local subscription data source.
        val subscriptionStore = SubscriptionStore(
            context = applicationContext
        )

        /**
         * Create the repository.
         *
         * RssEpisodeParser is not supplied because PodcastRepository
         * already provides it as a default constructor argument.
         */
        val repository = PodcastRepository(
            api = RetrofitClient.api,
            subscriptionStore = subscriptionStore
        )

        /**
         * Inject both the selected podcast and repository into
         * PodcastDetailViewModel.
         */
        return PodcastDetailViewModel(
            podcast = podcast,
            repository = repository
        ) as T
    }
}