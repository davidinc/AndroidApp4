package com.dawit.androidapp4.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dawit.androidapp4.data.model.Episode
import com.dawit.androidapp4.databinding.ItemEpisodeBinding

/**
 * Displays Episode objects inside the detail RecyclerView.
 */
class EpisodeAdapter(
    private val onEpisodeClicked: (Episode) -> Unit
) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EPISODE_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = getItem(position)
        holder.bind(episode)
    }

    inner class EpisodeViewHolder(
        private val binding: ItemEpisodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: Episode) {
            binding.episodeTitle.text = episode.title
            binding.episodeDate.text = episode.publicationDate
            binding.episodeDuration.text = episode.duration

            binding.root.setOnClickListener {
                onEpisodeClicked(episode)
            }
        }
    }

    companion object {
        private val EPISODE_COMPARATOR = object : DiffUtil.ItemCallback<Episode>() {
            override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
                return oldItem.audioUrl == newItem.audioUrl
            }

            override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
                return oldItem == newItem
            }
        }
    }
}
