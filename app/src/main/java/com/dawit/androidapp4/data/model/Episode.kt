package com.dawit.androidapp4.data.model

/**
 * Represents one playable episode parsed from a podcast RSS feed.
 */
data class Episode(
    val title: String,
    val description: String,
    val publicationDate: String,
    val duration: String,
    val audioUrl: String
)