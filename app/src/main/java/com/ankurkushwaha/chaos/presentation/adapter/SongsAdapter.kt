package com.ankurkushwaha.chaos.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ankurkushwaha.chaos.R
import com.ankurkushwaha.chaos.data.model.Song
import com.ankurkushwaha.chaos.utils.formatDuration
import com.bumptech.glide.Glide

class SongsAdapter(
    private val context: Context,
    private val menuClickListener: OnSongMenuClickListener
) : ListAdapter<Song, SongsAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_holder_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgSong: ImageView = itemView.findViewById(R.id.imgSong)
        private val tvSongTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
        private val tvSongArtist: TextView = itemView.findViewById(R.id.tvSongArtist)
        private val tvSongDuration: TextView = itemView.findViewById(R.id.tvSongDuration)
        private val layout: LinearLayout = itemView.findViewById(R.id.itemLayout)
        private val imgMore: ImageView = itemView.findViewById(R.id.imgMore)

        fun bind(song: Song) {
            tvSongTitle.text = song.title
            tvSongArtist.text = song.artist
            tvSongDuration.text = song.duration.formatDuration()

            // Handle song item click
            layout.setOnClickListener {
                menuClickListener.onSongClick(song)
            }

            // Load album art using Glide
            Glide.with(context)
                .load(song.imageUri)
                .placeholder(R.drawable.music)
                .into(imgSong)

            // Show PopupMenu on imgMore click
            imgMore.setOnClickListener {
                showPopupMenu(context, it, song)
            }
        }
    }

    private fun showPopupMenu(context: Context, view: View, song: Song) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.song_menu)

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_playNext -> {
                    menuClickListener.onPlayNext(song)
                    true
                }

                R.id.action_add_to_playlist -> {
                    menuClickListener.onAddToPlaylist(song)
                    true
                }

                R.id.action_details -> {
                    menuClickListener.onDetails(song)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}


private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}