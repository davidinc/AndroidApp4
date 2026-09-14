package com.dawit.androidapp4.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dawit.androidapp4.R
import com.dawit.androidapp4.data.model.Podcast
import com.dawit.androidapp4.databinding.ItemPodcastBinding

/**
 * Displays Podcast objects inside RecyclerView.
 *
 * Primary constructor:
 * The adapter receives a function that will be called when the user
 * selects a podcast.
 *
 * (Podcast) -> Unit means:
 * - receive one Podcast
 * - perform an action
 * - return no value
 */
class PodcastAdapter(
    private val onPodcastClicked: (Podcast) -> Unit
) : ListAdapter<
        Podcast,
        PodcastAdapter.PodcastViewHolder
        >(PODCAST_COMPARATOR) {

    /**
     * Creates a new row when RecyclerView needs one.
     *
     * ItemPodcastBinding is automatically generated from
     * item_podcast.xml because View Binding is enabled.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PodcastViewHolder {

        // LayoutInflater converts XML into Android View objects.
        val binding = ItemPodcastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PodcastViewHolder(binding)
    }

    /**
     * Connects a Podcast object to an existing row.
     *
     * position identifies which podcast should be displayed.
     */
    override fun onBindViewHolder(
        holder: PodcastViewHolder,
        position: Int
    ) {
        val podcast = getItem(position)
        holder.bind(podcast)
    }

    /**
     * ViewHolder stores the views belonging to one RecyclerView row.
     *
     * RecyclerView reuses ViewHolder objects instead of creating
     * completely new rows every time the user scrolls.
     */
    inner class PodcastViewHolder(
        private val binding: ItemPodcastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Places one Podcast object's information into the XML views.
         */
        fun bind(podcast: Podcast) {

            // Display the podcast title.
            binding.podcastTitle.text =
                podcast.collectionName

            // Display the podcast creator.
            binding.podcastArtist.text =
                podcast.artistName

            /**
             * Replace the placeholders in podcast_metadata:
             *
             * %1$d = episode count
             * %2$s = podcast category
             * %3$d = number of words in the title
             */
            binding.podcastMetadata.text =
                binding.root.context.getString(
                    R.string.podcast_metadata,
                    podcast.trackCount,
                    podcast.primaryGenreName
                        ?: binding.root.context.getString(
                            R.string.unknown_genre
                        ),
                    podcast.titleWordCount
                )

            /**
             * Glide downloads and displays the podcast artwork.
             *
             * The launcher image appears temporarily while the
             * artwork is loading or when downloading fails.
             */
            Glide.with(binding.podcastArtwork)
                .load(podcast.artworkUrl)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(binding.podcastArtwork)

            /**
             * Notify MainActivity when this podcast row is selected.
             */
            binding.root.setOnClickListener {
                onPodcastClicked(podcast)
            }
        }
    }

    /**
     * Companion-object members belong to the adapter class itself.
     */
    private companion object {

        /**
         * DiffUtil compares the old and new podcast lists.
         *
         * RecyclerView then updates only rows that changed instead
         * of rebuilding the complete list.
         */
        val PODCAST_COMPARATOR =
            object : DiffUtil.ItemCallback<Podcast>() {

                /**
                 * Two results represent the same podcast when their
                 * unique Apple track IDs are equal.
                 */
                override fun areItemsTheSame(
                    oldItem: Podcast,
                    newItem: Podcast
                ): Boolean {
                    return oldItem.trackId == newItem.trackId
                }

                /**
                 * Checks whether all displayed podcast values are equal.
                 */
                override fun areContentsTheSame(
                    oldItem: Podcast,
                    newItem: Podcast
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}