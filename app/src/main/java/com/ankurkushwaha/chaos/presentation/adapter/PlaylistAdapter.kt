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
import com.ankurkushwaha.chaos.data.model.Playlist

class PlaylistAdapter(
    private val context: Context,
    private val menuClickListener: OnPlaylistMenuClickListener
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.view_holder_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPlaylistTitle: TextView = itemView.findViewById(R.id.tvPlaylistTitle)
        private val layout: LinearLayout = itemView.findViewById(R.id.itemLayout)
        private val imgMore: ImageView = itemView.findViewById(R.id.imgMore)

        fun bind(playlist: Playlist) {
            tvPlaylistTitle.text = playlist.name

            // Handle song item click
            layout.setOnClickListener {
                menuClickListener.onPlaylistClick(playlist)
            }

            // Show PopupMenu on imgMore click
            imgMore.setOnClickListener {
                showPopupMenu(context, it, playlist)
            }
        }
    }

    private fun showPopupMenu(context: Context, view: View, playlist: Playlist) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.playlist_menu)

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_removePlaylist -> {
                    menuClickListener.onRemoveClick(playlist)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}


private class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.playlistId == newItem.playlistId
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }
}