package com.uniandes.tsdc.vinilos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uniandes.tsdc.vinilos.databinding.ItemAlbumBinding
import com.uniandes.tsdc.vinilos.models.Album

class AlbumsAdapter(private val onItemClick: (Album) -> Unit) :
    ListAdapter<Album, AlbumsAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.textAlbumName.text = album.name
            binding.textAlbumGenre.text = album.genre
            binding.textAlbumReleaseDate.text = album.releaseDate
            Glide.with(binding.root)
                .load(album.cover)
                .into(binding.imageAlbumCover)
            binding.root.setOnClickListener { onItemClick(album) }
        }
    }

    class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Album, newItem: Album) = oldItem == newItem
    }
}
