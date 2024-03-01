package com.pragmo.kyeootomi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ItemDocumentBinding
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item

class ItemAdapter(private var items : List<Item>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal class ItemViewHolder(val binding : ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ItemViewHolder).binding
        val item = items[position]
        binding.txtTitle.text = item.title
        when (item.type) {
            "hitomi" -> {
                val hitomiItem = item as HitomiItem
                binding.imgIcon.setImageResource(R.drawable.ic_hitomi)
            }
            else -> {
                val customItem = item as CustomItem
                binding.imgIcon.setImageResource(R.drawable.ic_custom)
            }
        }
    }
}