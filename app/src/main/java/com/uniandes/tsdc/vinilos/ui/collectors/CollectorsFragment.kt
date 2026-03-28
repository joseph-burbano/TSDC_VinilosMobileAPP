package com.uniandes.tsdc.vinilos.ui.collectors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniandes.tsdc.vinilos.adapters.CollectorsAdapter
import com.uniandes.tsdc.vinilos.databinding.FragmentCollectorsBinding
import com.uniandes.tsdc.vinilos.viewmodels.CollectorViewModel

class CollectorsFragment : Fragment() {

    private var _binding: FragmentCollectorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CollectorViewModel
    private lateinit var adapter: CollectorsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CollectorViewModel::class.java]
        adapter = CollectorsAdapter()
        binding.recyclerCollectors.layoutManager = LinearLayoutManager(context)
        binding.recyclerCollectors.adapter = adapter

        viewModel.collectors.observe(viewLifecycleOwner) { collectors ->
            collectors?.let {
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
