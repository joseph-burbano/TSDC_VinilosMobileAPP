package com.uniandes.tsdc.vinilos.ui.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.uniandes.tsdc.vinilos.R
import com.uniandes.tsdc.vinilos.databinding.FragmentArtistDetailBinding

class ArtistDetailFragment : Fragment() {

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!
    private val args: ArtistDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val artist = args.artist

        binding.textArtistName.text = artist.name
        binding.textArtistBirthDate.text = getString(R.string.birth_date, artist.birthDate)
        binding.textArtistDescription.text = artist.description

        Glide.with(this)
            .load(artist.image)
            .into(binding.imageArtist)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
