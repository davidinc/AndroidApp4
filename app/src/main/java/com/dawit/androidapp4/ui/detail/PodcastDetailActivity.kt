package com.dawit.androidapp4.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.dawit.androidapp4.R
import com.dawit.androidapp4.data.model.Episode
import com.dawit.androidapp4.data.model.Podcast
import com.dawit.androidapp4.databinding.ActivityPodcastDetailBinding
import kotlinx.coroutines.launch

/**
 * Displays information and episodes for one selected podcast.
 *
 * Android constructs Activity classes, so we do not declare our own
 * constructor for PodcastDetailActivity.
 *
 * The selected Podcast is received through Intent extras.
 */
class PodcastDetailActivity : AppCompatActivity() {

    /**
     * View Binding class generated from activity_podcast_detail.xml.
     */
    private lateinit var binding: ActivityPodcastDetailBinding

    /**
     * ExoPlayer is nullable because it exists only while this
     * Activity is visible.
     */
    private var player: ExoPlayer? = null

    /**
     * Convert the Intent extras into a Podcast object.
     *
     * "by lazy" delays this operation until podcast is first used.
     * Android will have already provided the Activity's Intent.
     */
    private val podcast: Podcast by lazy {
        podcastFromIntent(intent)
    }

    /**
     * The factory receives the selected podcast and constructs
     * PodcastDetailViewModel with its repository.
     */
    private val viewModel: PodcastDetailViewModel by viewModels {
        PodcastDetailViewModelFactory(
            context = applicationContext,
            podcast = podcast
        )
    }

    /**
     * EpisodeAdapter returns the selected Episode to playEpisode().
     */
    private val episodeAdapter = EpisodeAdapter { selectedEpisode ->
        playEpisode(selectedEpisode)
    }

    /**
     * Android calls onCreate when the detail screen is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Convert the XML layout into View objects.
        binding = ActivityPodcastDetailBinding.inflate(
            layoutInflater
        )

        // Display activity_podcast_detail.xml.
        setContentView(binding.root)

        // Display the selected podcast's title, creator and artwork.
        displayPodcastInformation()

        // Connect EpisodeAdapter to the episode RecyclerView.
        configureEpisodeList()

        // Connect the subscription button to the ViewModel.
        configureSubscriptionButton()

        // Observe episode and subscription state changes.
        observeViewModel()
    }

    /**
     * Displays podcast information received from MainActivity.
     */
    private fun displayPodcastInformation() {

        // Display the podcast name.
        binding.podcastTitle.text =
            podcast.collectionName

        // Display the podcast creator.
        binding.podcastArtist.text =
            podcast.artistName

        // Download and display the podcast artwork.
        Glide.with(this)
            .load(podcast.artworkUrl)
            .centerCrop()
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.podcastArtwork)
    }

    /**
     * Connects EpisodeAdapter to RecyclerView.
     */
    private fun configureEpisodeList() {
        binding.episodeList.apply {

            // Display episodes vertically.
            layoutManager = LinearLayoutManager(
                this@PodcastDetailActivity
            )

            // Connect RecyclerView to EpisodeAdapter.
            adapter = episodeAdapter

            // Episode row dimensions remain stable while scrolling.
            setHasFixedSize(true)
        }
    }

    /**
     * Connects the Subscribe button to the ViewModel.
     */
    private fun configureSubscriptionButton() {
        binding.subscribeButton.setOnClickListener {
            viewModel.toggleSubscription()
        }
    }

    /**
     * Observes the PodcastDetailUiState StateFlow.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->

                    // Display or hide the RSS loading indicator.
                    binding.detailLoadingIndicator.isVisible =
                        state.isLoading

                    // Display instructions, confirmations or errors.
                    binding.detailStatusText.text =
                        state.message

                    // Change the button according to subscription state.
                    binding.subscribeButton.text =
                        getString(
                            if (state.isSubscribed) {
                                R.string.unsubscribe
                            } else {
                                R.string.subscribe
                            }
                        )

                    // Send the latest episode list to RecyclerView.
                    episodeAdapter.submitList(
                        state.episodes
                    )
                }
            }
        }
    }

    /**
     * Android calls onStart when this Activity becomes visible.
     */
    override fun onStart() {
        super.onStart()

        /**
         * Create one ExoPlayer instance and connect it to PlayerView.
         *
         * The Elvis-style null check prevents creating a second
         * player when one already exists.
         */
        if (player == null) {
            player = ExoPlayer.Builder(this)
                .build()
                .also { createdPlayer ->
                    binding.playerView.player = createdPlayer
                }
        }
    }

    /**
     * Plays the episode selected in EpisodeAdapter.
     */
    private fun playEpisode(episode: Episode) {

        /**
         * MediaMetadata provides the title and podcast name to
         * Android's player controls.
         */
        val metadata = MediaMetadata.Builder()
            .setTitle(episode.title)
            .setArtist(podcast.collectionName)
            .build()

        /**
         * MediaItem contains the playable audio URL and metadata.
         */
        val mediaItem = MediaItem.Builder()
            .setUri(episode.audioUrl)
            .setMediaMetadata(metadata)
            .build()

        /**
         * The safe-call operator means this block runs only when
         * ExoPlayer has been created.
         */
        player?.apply {

            // Replace any previously selected episode.
            setMediaItem(mediaItem)

            // Prepare the internet audio stream.
            prepare()

            // Begin playback.
            play()
        }

        // Display the selected episode title below the player.
        binding.nowPlayingText.text =
            getString(
                R.string.now_playing,
                episode.title
            )
    }

    /**
     * Android calls onStop when the Activity is no longer visible.
     */
    override fun onStop() {

        /**
         * Disconnect PlayerView and release ExoPlayer resources.
         *
         * Releasing the player prevents decoder, network and
         * memory leaks.
         */
        binding.playerView.player = null
        player?.release()
        player = null

        super.onStop()
    }

    /**
     * Companion-object members belong to PodcastDetailActivity
     * instead of one particular Activity instance.
     */
    companion object {

        /**
         * Intent-extra keys.
         *
         * Private constants prevent spelling differences when values
         * are added and retrieved.
         */
        private const val EXTRA_ID = "podcast_id"
        private const val EXTRA_TITLE = "podcast_title"
        private const val EXTRA_ARTIST = "podcast_artist"
        private const val EXTRA_ARTWORK_100 = "podcast_artwork_100"
        private const val EXTRA_ARTWORK_600 = "podcast_artwork_600"
        private const val EXTRA_FEED = "podcast_feed"
        private const val EXTRA_COUNT = "podcast_episode_count"
        private const val EXTRA_GENRE = "podcast_genre"
        private const val EXTRA_RELEASE_DATE = "podcast_release_date"

        /**
         * Constructs the Intent used by MainActivity.
         *
         * Context identifies where the navigation starts.
         * PodcastDetailActivity::class.java identifies the destination.
         */
        fun newIntent(
            context: Context,
            podcast: Podcast
        ): Intent {
            return Intent(
                context,
                PodcastDetailActivity::class.java
            ).apply {

                // Place the Podcast fields inside the Intent.
                putExtra(EXTRA_ID, podcast.trackId)
                putExtra(EXTRA_TITLE, podcast.collectionName)
                putExtra(EXTRA_ARTIST, podcast.artistName)
                putExtra(EXTRA_ARTWORK_100, podcast.artworkUrl100)
                putExtra(EXTRA_ARTWORK_600, podcast.artworkUrl600)
                putExtra(EXTRA_FEED, podcast.feedUrl)
                putExtra(EXTRA_COUNT, podcast.trackCount)
                putExtra(EXTRA_GENRE, podcast.primaryGenreName)
                putExtra(
                    EXTRA_RELEASE_DATE,
                    podcast.releaseDate
                )
            }
        }

        /**
         * Reconstructs a Podcast from the received Intent extras.
         *
         * This avoids adding Parcelable configuration for this
         * introductory assignment.
         */
        private fun podcastFromIntent(
            intent: Intent
        ): Podcast {
            return Podcast(
                trackId = intent.getLongExtra(
                    EXTRA_ID,
                    0
                ),
                collectionName = intent.getStringExtra(
                    EXTRA_TITLE
                ).orEmpty(),
                artistName = intent.getStringExtra(
                    EXTRA_ARTIST
                ).orEmpty(),
                artworkUrl100 = intent.getStringExtra(
                    EXTRA_ARTWORK_100
                ).orEmpty(),
                artworkUrl600 = intent.getStringExtra(
                    EXTRA_ARTWORK_600
                ),
                feedUrl = intent.getStringExtra(
                    EXTRA_FEED
                ),
                trackCount = intent.getIntExtra(
                    EXTRA_COUNT,
                    0
                ),
                primaryGenreName = intent.getStringExtra(
                    EXTRA_GENRE
                ),
                releaseDate = intent.getStringExtra(
                    EXTRA_RELEASE_DATE
                )
            )
        }
    }
}