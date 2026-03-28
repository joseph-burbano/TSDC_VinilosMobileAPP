package com.uniandes.tsdc.vinilos.ui.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniandes.tsdc.vinilos.adapters.ArtistsAdapter
import com.uniandes.tsdc.vinilos.databinding.FragmentArtistsBinding
import com.uniandes.tsdc.vinilos.viewmodels.ArtistViewModel

class ArtistsFragment : Fragment() {

    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ArtistViewModel
    private lateinit var adapter: ArtistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ArtistViewModel::class.java]
        adapter = ArtistsAdapter { artist ->
            val action = ArtistsFragmentDirections.actionArtistsFragmentToArtistDetailFragment(artist)
            findNavController().navigate(action)
        }
        binding.recyclerArtists.layoutManager = LinearLayoutManager(context)
        binding.recyclerArtists.adapter = adapter

        viewModel.artists.observe(viewLifecycleOwner) { artists ->
            artists?.let {
                adapter.submitList(it)
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.eventNetworkError.observe(viewLifecycleOwner) { isError ->
            if (isError && viewModel.isNetworkErrorShown.value == false) {
                Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
                viewModel.onNetworkErrorShown()
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshDataFromNetwork()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
