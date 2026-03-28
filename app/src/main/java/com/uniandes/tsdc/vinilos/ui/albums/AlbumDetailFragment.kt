package com.uniandes.tsdc.vinilos.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.uniandes.tsdc.vinilos.R
import com.uniandes.tsdc.vinilos.databinding.FragmentAlbumDetailBinding

class AlbumDetailFragment : Fragment() {

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!
    private val args: AlbumDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val album = args.album

        binding.textAlbumName.text = album.name
        binding.textAlbumGenre.text = getString(R.string.genre, album.genre)
        binding.textAlbumLabel.text = getString(R.string.record_label, album.recordLabel)
        binding.textAlbumReleaseDate.text = getString(R.string.release_date, album.releaseDate)
        binding.textAlbumDescription.text = album.description

        Glide.with(this)
            .load(album.cover)
            .into(binding.imageAlbumCover)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
