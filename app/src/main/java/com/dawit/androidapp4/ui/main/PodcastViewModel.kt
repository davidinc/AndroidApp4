package com.dawit.androidapp4.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dawit.androidapp4.data.model.Podcast
import com.dawit.androidapp4.data.repository.PodcastRepository
import com.dawit.androidapp4.domain.SearchMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Contains all information required to display the main podcast screen.
 *
 * A data class is useful here because every UI state is one immutable
 * collection of values.
 */
data class PodcastUiState(

    // True while a network search is running.
    val isLoading: Boolean = false,

    // Podcasts currently displayed by RecyclerView.
    val podcasts: List<Podcast> = emptyList(),

    // Instruction, result count, validation error, or network error.
    val message: String = "Search for a podcast topic to begin."
)

/**
 * Controls the data and business actions for the main screen.
 *
 * Primary constructor:
 * PodcastViewModel requires a PodcastRepository.
 *
 * We will create this ViewModel through a factory because Android cannot
 * automatically construct a ViewModel that has constructor parameters.
 */
class PodcastViewModel(
    private val repository: PodcastRepository
) : ViewModel() {

    /**
     * MutableStateFlow is private because only this ViewModel should
     * be allowed to change the screen state.
     */
    private val _state = MutableStateFlow(
        PodcastUiState()
    )

    /**
     * StateFlow is public but read-only.
     *
     * MainActivity will observe this property and update the screen
     * whenever its value changes.
     */
    val state: StateFlow<PodcastUiState> =
        _state.asStateFlow()

    /**
     * Searches Apple and applies the selected advanced filter.
     */
    fun search(
        query: String,
        mode: SearchMode,
        criteria: String
    ) {
        // Validate the main search field before making a network request.
        if (query.isBlank()) {
            _state.value = PodcastUiState(
                message = "Enter a podcast topic."
            )
            return
        }

        /**
         * viewModelScope starts a coroutine belonging to this ViewModel.
         *
         * Android automatically cancels the coroutine when the
         * ViewModel is permanently destroyed.
         */
        viewModelScope.launch {

            // Tell the Activity to display its progress indicator.
            _state.value = PodcastUiState(
                isLoading = true,
                message = "Searching Apple Podcasts..."
            )

            try {
                // Ask the repository to search and filter podcasts.
                val results = repository.search(
                    query = query.trim(),
                    mode = mode,
                    criteria = criteria.trim()
                )

                // Publish the successful result to the Activity.
                _state.value = PodcastUiState(
                    isLoading = false,
                    podcasts = results,
                    message = if (results.isEmpty()) {
                        "No podcasts matched this search and filter."
                    } else {
                        "${results.size} podcast(s) found."
                    }
                )
            } catch (error: IllegalArgumentException) {
                /**
                 * IllegalArgumentException normally means that the
                 * regex or minimum-word value was invalid.
                 */
                _state.value = PodcastUiState(
                    isLoading = false,
                    message = error.message
                        ?: "The filter value is invalid."
                )
            } catch (error: Exception) {
                /**
                 * Other exceptions can come from internet, server,
                 * JSON, or RSS problems.
                 */
                _state.value = PodcastUiState(
                    isLoading = false,
                    message = error.message
                        ?: "The podcast search failed."
                )
            }
        }
    }

    /**
     * Displays podcasts saved in SubscriptionStore instead of
     * performing an internet search.
     */
    fun showSubscriptions() {
        val savedPodcasts = repository.getSubscriptions()

        _state.value = PodcastUiState(
            isLoading = false,
            podcasts = savedPodcasts,
            message = if (savedPodcasts.isEmpty()) {
                "You have not subscribed to a podcast yet."
            } else {
                "${savedPodcasts.size} saved subscription(s)."
            }
        )
    }
}