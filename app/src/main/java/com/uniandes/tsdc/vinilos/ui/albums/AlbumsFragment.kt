package com.uniandes.tsdc.vinilos.ui.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniandes.tsdc.vinilos.adapters.AlbumsAdapter
import com.uniandes.tsdc.vinilos.databinding.FragmentAlbumsBinding
import com.uniandes.tsdc.vinilos.viewmodels.AlbumViewModel

class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AlbumViewModel
    private lateinit var adapter: AlbumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
        adapter = AlbumsAdapter { album ->
            val action = AlbumsFragmentDirections.actionAlbumsFragmentToAlbumDetailFragment(album)
            findNavController().navigate(action)
        }
        binding.recyclerAlbums.layoutManager = LinearLayoutManager(context)
        binding.recyclerAlbums.adapter = adapter

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            albums?.let {
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
