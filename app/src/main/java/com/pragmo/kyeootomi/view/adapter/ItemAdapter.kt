package com.pragmo.kyeootomi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentItemCustomBinding
import com.pragmo.kyeootomi.databinding.FragmentItemHitomiBinding
import com.pragmo.kyeootomi.databinding.ItemDocumentBinding
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.view.ToggleAnimation

class ItemAdapter(private var items : List<Item>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal class ItemViewHolder(val binding : ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDocumentBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ItemViewHolder).binding
        val inflater = LayoutInflater.from(binding.root.context)
        val item = items[position]
        binding.txtTitle.text = item.title
        when (item.type) {
            "hitomi" -> {
                val hitomiItem = item as HitomiItem

                // 세부사항 페이지 설정
                val fragmentHitomiBinding = FragmentItemHitomiBinding.inflate(inflater, binding.root, false)
                fragmentHitomiBinding.btnWebview.setOnClickListener {
                    Toast.makeText(binding.root.context, "히토미 보기 페이지로 넘어가야함", Toast.LENGTH_SHORT).show()
                }
                binding.wrapview.addView(fragmentHitomiBinding.root)
                binding.imgIcon.setImageResource(R.drawable.ic_hitomi)
            }
            else -> {
                val customItem = item as CustomItem
                val fragmentCustomBinding = FragmentItemCustomBinding.inflate(inflater, binding.root, false)
                fragmentCustomBinding.btnWebview.setOnClickListener {
                    Toast.makeText(binding.root.context, "웹뷰 페이지로 넘어가야함", Toast.LENGTH_SHORT).show()
                }
                binding.wrapview.addView(fragmentCustomBinding.root)
                binding.imgIcon.setImageResource(R.drawable.ic_custom)
            }
        }

        // 항목 펼침/접힘 애니메이션 설정
        var isExpanded = false
        binding.rootMain.setOnClickListener {
            if (isExpanded)
                ToggleAnimation.collapse(binding.wrapview)
            else
                ToggleAnimation.expand(binding.wrapview)

            isExpanded = !isExpanded
            ToggleAnimation.toggleArrow(binding.imgMore, isExpanded)
        }
    }
}