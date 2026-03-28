package com.uniandes.tsdc.vinilos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uniandes.tsdc.vinilos.databinding.ItemCollectorBinding
import com.uniandes.tsdc.vinilos.models.Collector

class CollectorsAdapter :
    ListAdapter<Collector, CollectorsAdapter.CollectorViewHolder>(CollectorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectorViewHolder {
        val binding = ItemCollectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CollectorViewHolder(private val binding: ItemCollectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(collector: Collector) {
            binding.textCollectorName.text = collector.name
            binding.textCollectorEmail.text = collector.email
            binding.textCollectorPhone.text = collector.telephone
        }
    }

    class CollectorDiffCallback : DiffUtil.ItemCallback<Collector>() {
        override fun areItemsTheSame(oldItem: Collector, newItem: Collector) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Collector, newItem: Collector) = oldItem == newItem
    }
}
