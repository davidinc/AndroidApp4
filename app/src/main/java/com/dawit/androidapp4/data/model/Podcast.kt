package com.dawit.androidapp4.data.model

/**
 * Represents one podcast returned by the Apple iTunes Search API.
 */
data class Podcast(
    val trackId: Long = 0,
    val collectionName: String = "Untitled podcast",
    val artistName: String = "Unknown creator",
    val artworkUrl100: String = "",
    val artworkUrl600: String? = null,
    val feedUrl: String? = null,
    val trackCount: Int = 0,
    val primaryGenreName: String? = null,
    val releaseDate: String? = null
) {
    /**
     * Uses the larger artwork when available.
     */
    val artworkUrl: String
        get() = artworkUrl600?.takeIf { it.isNotBlank() } ?: artworkUrl100

    /**
     * Calculates the number of words in the podcast title.
     */
    val titleWordCount: Int
        get() = collectionName
            .trim()
            .split(Regex("\\s+"))
            .count { it.isNotBlank() }
}

/**
 * Represents the complete JSON response returned by Apple.
 */
data class PodcastResponse(
    val resultCount: Int = 0,
    val results: List<Podcast> = emptyList()
)