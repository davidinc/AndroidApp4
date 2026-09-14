package com.dawit.androidapp4.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dawit.androidapp4.data.model.Episode
import com.dawit.androidapp4.data.model.Podcast
import com.dawit.androidapp4.data.repository.PodcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Contains all information displayed by the podcast-detail screen.
 */
data class PodcastDetailUiState(

    // True while the RSS feed is being downloaded and parsed.
    val isLoading: Boolean = false,

    // Playable episodes parsed from the RSS feed.
    val episodes: List<Episode> = emptyList(),

    // True when the selected podcast is saved as a subscription.
    val isSubscribed: Boolean = false,

    // Loading instruction, success information, or error message.
    val message: String = ""
)

/**
 * Controls the data and actions for one selected podcast.
 *
 * Primary constructor:
 *
 * podcast:
 * The Podcast selected on the main screen.
 *
 * repository:
 * Provides RSS episodes and subscription operations.
 */
class PodcastDetailViewModel(
    private val podcast: Podcast,
    private val repository: PodcastRepository
) : ViewModel() {

    /**
     * Create the initial state.
     *
     * SubscriptionStore is checked immediately to determine the
     * correct Subscribe or Unsubscribe button text.
     */
    private val _state = MutableStateFlow(
        PodcastDetailUiState(
            isSubscribed = repository.isSubscribed(
                podcast.trackId
            )
        )
    )

    // Public read-only state observed by PodcastDetailActivity.
    val state: StateFlow<PodcastDetailUiState> =
        _state.asStateFlow()

    /**
     * An init block runs immediately after the constructor finishes.
     *
     * Therefore, episodes begin loading as soon as this ViewModel
     * is created.
     */
    init {
        loadEpisodes()
    }

    /**
     * Adds or removes the selected podcast from local subscriptions.
     */
    fun toggleSubscription() {

        // Repository returns the podcast's new subscription state.
        val isNowSubscribed =
            repository.toggleSubscription(podcast)

        /**
         * copy() creates a new state while preserving values that
         * we do not explicitly change, such as the episode list.
         */
        _state.value = _state.value.copy(
            isSubscribed = isNowSubscribed,
            message = if (isNowSubscribed) {
                "Podcast subscribed."
            } else {
                "Podcast unsubscribed."
            }
        )
    }

    /**
     * Downloads and parses episodes from the selected podcast's RSS feed.
     */
    fun loadEpisodes() {

        // Obtain the RSS address returned by Apple.
        val feedUrl = podcast.feedUrl

        /**
         * Some Apple results may not contain a feed URL.
         * Stop before making a network request in that situation.
         */
        if (feedUrl.isNullOrBlank()) {
            _state.value = _state.value.copy(
                isLoading = false,
                message = "This podcast does not provide an RSS feed."
            )
            return
        }

        // Start an asynchronous operation belonging to this ViewModel.
        viewModelScope.launch {

            // Keep subscription status but begin showing progress.
            _state.value = _state.value.copy(
                isLoading = true,
                message = "Loading podcast episodes..."
            )

            try {
                // Download and parse the RSS feed through the repository.
                val loadedEpisodes =
                    repository.getEpisodes(feedUrl)

                // Publish the episode list to the detail Activity.
                _state.value = _state.value.copy(
                    isLoading = false,
                    episodes = loadedEpisodes,
                    message = if (loadedEpisodes.isEmpty()) {
                        "No playable audio episodes were found."
                    } else {
                        "Select an episode to begin playback."
                    }
                )
            } catch (error: Exception) {

                /**
                 * Display RSS, internet, or parsing errors without
                 * crashing the application.
                 */
                _state.value = _state.value.copy(
                    isLoading = false,
                    message = error.message
                        ?: "The podcast episodes could not be loaded."
                )
            }
        }
    }
}