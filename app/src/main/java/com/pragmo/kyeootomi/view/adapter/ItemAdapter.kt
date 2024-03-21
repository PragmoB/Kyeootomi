package com.pragmo.kyeootomi.view.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentItemCustomBinding
import com.pragmo.kyeootomi.databinding.FragmentItemHitomiBinding
import com.pragmo.kyeootomi.databinding.ItemDocumentBinding
import com.pragmo.kyeootomi.databinding.TagHitomiBinding
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.view.ToggleAnimation
import com.pragmo.kyeootomi.view.activity.HitomiViewActivity
import java.io.File

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
        binding.txtTitle.text = item.title?:"제목을 불러올 수 없습니다"
        when (item.type) {
            "hitomi" -> {
                val hitomiItem = item as HitomiItem
                binding.imgIcon.setImageResource(R.drawable.ic_hitomi)

                /* 세부사항 페이지 설정 */

                val fragmentHitomiBinding = FragmentItemHitomiBinding.inflate(inflater, binding.root, false)
                fragmentHitomiBinding.btnWebview.setOnClickListener {
                    Toast.makeText(binding.root.context, "히토미 보기 페이지로 넘어가야함", Toast.LENGTH_SHORT).show()
                }
                fragmentHitomiBinding.txtNumber.text = "번호: ${hitomiItem.number}"
                fragmentHitomiBinding.txtWriter.text = "작가: ${hitomiItem.artist?:"불러올 수 없음"}"
                fragmentHitomiBinding.txtSeries.text = "시리즈: ${hitomiItem.series?:"불러올 수 없음"}"
                hitomiItem.tags?.let { tags ->
                    for (tag in tags) {
                        val viewTag = TagHitomiBinding.inflate(inflater, fragmentHitomiBinding.root, false)
                        viewTag.rootMain.text = tag
                        fragmentHitomiBinding.tags.addView(viewTag.root)
                    }
                }
                fragmentHitomiBinding.imgCover.setOnClickListener {
                    val intentHitomiView = Intent(binding.root.context, HitomiViewActivity::class.java)
                    intentHitomiView.putExtra("_no", hitomiItem._no)
                    binding.root.context.startActivity(intentHitomiView)
                }
                val cover = hitomiItem.getFile(1)
                if (cover != null) {
                    fragmentHitomiBinding.wrapCoverError.visibility = View.GONE
                    fragmentHitomiBinding.imgCover.visibility = View.VISIBLE
                    fragmentHitomiBinding.imgCover.setImageURI(Uri.fromFile(cover))
                }
                binding.wrapview.addView(fragmentHitomiBinding.root)
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

        // wrapviewHeight = 펼쳐질 항목의 높이 측정 ) 사실 view.measure로 측정했었지만 (이 문제와는 별개로 표시되는 히토미 작품의 정보량이 너무 많아서 높이 300dp를 넘어 넘쳐버리는 일 때문에)
        // xml 레이아웃좀 수정했더니 맛가버림 왜그런지는 5시간정도 고민해보고 검색해보고 했는데 미스터리임.
        // 그런데 화면에 뜨는건 또 정상적으로 떠서 그냥 이거 이용하면 되겠다 싶어서 화면에 출력된 높이 구해오는 방법으로 했음
        var wrapviewHeight : Int = 800
        binding.wrapview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                wrapviewHeight = binding.wrapview.height
                binding.wrapview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                binding.wrapview.visibility = View.GONE
            }
        })

        // 항목 펼침/접힘 애니메이션 설정
        var isExpanded = false
        binding.rootMain.setOnClickListener {
            if (isExpanded)
                ToggleAnimation.collapse(binding.wrapview)
            else
                ToggleAnimation.expand(binding.wrapview, wrapviewHeight)

            isExpanded = !isExpanded
            ToggleAnimation.toggleArrow(binding.imgMore, isExpanded)
        }
    }
}