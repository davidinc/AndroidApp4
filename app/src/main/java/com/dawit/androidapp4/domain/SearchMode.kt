package com.dawit.androidapp4.domain

import com.dawit.androidapp4.data.model.Podcast

/**
 * Search and filtering choices displayed to the user.
 */
enum class SearchMode(val label: String) {
    STANDARD("Standard search"),
    REGEX_TITLE("Title regular expression"),
    MINIMUM_WORDS("Minimum title words"),
    FEWEST_EPISODES("Fewest episodes first"),
    NEWEST_FIRST("Newest first")
}

/**
 * Applies advanced criteria after Apple returns the podcast results.
 *
 * This logic is separate from the Activity so it can be tested independently.
 */
object SearchFilterEngine {

    fun apply(
        podcasts: List<Podcast>,
        mode: SearchMode,
        criteria: String
    ): List<Podcast> {

        return when (mode) {
            SearchMode.STANDARD -> podcasts

            SearchMode.REGEX_TITLE -> {
                require(criteria.isNotBlank()) {
                    "Enter a regular expression, for example ^The.*"
                }

                val expression = try {
                    Regex(criteria, RegexOption.IGNORE_CASE)
                } catch (error: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid regular expression: ${error.message}"
                    )
                }

                podcasts.filter { podcast ->
                    expression.containsMatchIn(podcast.collectionName) ||
                            expression.containsMatchIn(podcast.artistName)
                }
            }

            SearchMode.MINIMUM_WORDS -> {
                val minimum = criteria.toIntOrNull()
                    ?: throw IllegalArgumentException(
                        "Enter a valid whole number."
                    )

                require(minimum > 0) {
                    "Enter a whole number greater than zero."
                }

                podcasts.filter { podcast ->
                    podcast.titleWordCount >= minimum
                }
            }

            SearchMode.FEWEST_EPISODES -> {
                podcasts.sortedBy { podcast ->
                    if (podcast.trackCount > 0) {
                        podcast.trackCount
                    } else {
                        Int.MAX_VALUE
                    }
                }
            }

            SearchMode.NEWEST_FIRST -> {
                podcasts.sortedByDescending { podcast ->
                    podcast.releaseDate.orEmpty()
                }
            }
        }
    }
}