package com.dawit.androidapp4.data.remote

import android.util.Xml
import com.dawit.androidapp4.data.model.Episode
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

/**
 * Converts a podcast RSS XML file into a list of Episode objects.
 *
 * Constructor:
 * This class uses Kotlin's default empty constructor because it does not
 * require any information when the object is created.
 *
 * Example:
 * val parser = RssEpisodeParser()
 */
class RssEpisodeParser {

    /**
     * Reads the supplied RSS input stream and returns playable episodes.
     *
     * An InputStream represents incoming data from the internet.
     */
    fun parse(inputStream: InputStream): List<Episode> {

        /**
         * Creates Android's streaming XML parser.
         *
         * A streaming parser reads one XML element at a time instead of
         * loading the complete document into memory.
         */
        val parser = Xml.newPullParser().apply {

            // Keep prefixes such as "itunes:duration" available.
            setFeature(
                XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                false
            )

            // Connect the XML parser to the incoming RSS data.
            setInput(inputStream, null)
        }

        // This mutable list collects the episodes found in the RSS document.
        val episodes = mutableListOf<Episode>()

        // True while the parser is reading content inside an <item>.
        var insideItem = false

        // Stores the name of the text element currently being read.
        var activeTag: String? = null

        // Collects text that can arrive in multiple pieces.
        val collectedText = StringBuilder()

        // Temporary values for the episode currently being parsed.
        var title = "Untitled episode"
        var description = ""
        var publicationDate = ""
        var duration = ""
        var audioUrl = ""

        // Obtain the first event from the XML document.
        var eventType = parser.eventType

        /**
         * Continue until the end of the XML document.
         *
         * The second condition limits the result to 50 episodes so a very
         * large RSS feed does not consume unnecessary memory.
         */
        while (
            eventType != XmlPullParser.END_DOCUMENT &&
            episodes.size < MAX_EPISODES
        ) {
            when (eventType) {

                /**
                 * START_TAG represents an opening XML tag such as:
                 * <item>, <title>, or <enclosure>.
                 */
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                        .substringAfter(':')
                        .lowercase()

                    if (tagName == "item") {
                        // A new podcast episode has started.
                        insideItem = true

                        // Reset values left by the previous episode.
                        title = "Untitled episode"
                        description = ""
                        publicationDate = ""
                        duration = ""
                        audioUrl = ""
                    } else if (
                        insideItem &&
                        tagName == "enclosure"
                    ) {
                        /**
                         * A podcast enclosure normally looks like:
                         *
                         * <enclosure
                         *     url="https://example.com/episode.mp3"
                         *     type="audio/mpeg" />
                         */
                        audioUrl = parser
                            .getAttributeValue(null, "url")
                            .orEmpty()
                    } else if (
                        insideItem &&
                        tagName in TEXT_TAGS
                    ) {
                        // Start collecting text for this particular tag.
                        activeTag = tagName
                        collectedText.clear()
                    }
                }

                /**
                 * TEXT and CDSECT represent text between XML tags.
                 *
                 * CDATA is commonly used when descriptions contain HTML.
                 */
                XmlPullParser.TEXT,
                XmlPullParser.CDSECT -> {
                    if (insideItem && activeTag != null) {
                        collectedText.append(parser.text)
                    }
                }

                /**
                 * END_TAG represents a closing tag such as </title>.
                 */
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                        .substringAfter(':')
                        .lowercase()

                    if (insideItem && activeTag == tagName) {
                        val value = collectedText.toString().trim()

                        // Save the collected value in the correct variable.
                        when (tagName) {
                            "title" -> {
                                if (value.isNotBlank()) {
                                    title = value
                                }
                            }

                            "description",
                            "summary" -> {
                                if (description.isBlank()) {
                                    description = cleanDescription(value)
                                }
                            }

                            "pubdate" -> publicationDate = value

                            "duration" -> duration = value
                        }

                        // Text collection for this tag is complete.
                        activeTag = null
                    }

                    if (tagName == "item" && insideItem) {

                        /**
                         * Only add episodes that contain a playable audio URL.
                         * RSS items without an audio enclosure are ignored.
                         */
                        if (audioUrl.isNotBlank()) {
                            episodes.add(
                                Episode(
                                    title = title,
                                    description = description,
                                    publicationDate = publicationDate,
                                    duration = duration,
                                    audioUrl = audioUrl
                                )
                            )
                        }

                        // The current episode has ended.
                        insideItem = false
                        activeTag = null
                    }
                }
            }

            // Move to the next XML event.
            eventType = parser.next()
        }

        return episodes
    }

    /**
     * Removes basic HTML tags and repeated spaces from descriptions.
     */
    private fun cleanDescription(value: String): String {
        return value
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Companion-object values belong to the class rather than one instance.
     * They are private because they are only used by this parser.
     */
    private companion object {

        // Maximum number of playable episodes loaded from one RSS feed.
        const val MAX_EPISODES = 50

        // RSS elements whose text we want to collect.
        val TEXT_TAGS = setOf(
            "title",
            "description",
            "summary",
            "pubdate",
            "duration"
        )
    }
}