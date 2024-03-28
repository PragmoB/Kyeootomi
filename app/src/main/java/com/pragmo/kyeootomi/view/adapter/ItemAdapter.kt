package com.pragmo.kyeootomi.view.adapter

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ItemCustomBinding
import com.pragmo.kyeootomi.databinding.ItemHitomiBinding
import com.pragmo.kyeootomi.databinding.TagHitomiBinding
import com.pragmo.kyeootomi.model.data.CustomItem
import com.pragmo.kyeootomi.model.data.HitomiItem
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.view.ToggleAnimation
import com.pragmo.kyeootomi.view.activity.item.read.ReadHitomiActivity

class ItemAdapter(private var items : List<Item>, private val onLongClickItemListener: (ItemAdapter) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val checkViews = Array<CheckBox?>(items.size) { null }
    var selectMode = false
        set(value) {
            if (field == value)
                return

            // selectMode 변경 시 checkView 나타내기/숨기기, 값 바꾸기 작업
            field = value
            for (checkView in checkViews) {
                checkView ?: return
                if (field) {
                    // 체크박스 나타나기 애니메이션
                    checkView.visibility = View.INVISIBLE
                    checkView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
                        override fun onGlobalLayout() {
                            Handler(Looper.getMainLooper()).postDelayed({
                                checkView.visibility = View.VISIBLE
                                val appearAnim = AnimationUtils.loadAnimation(checkView.context, R.anim.appear)
                                checkView.startAnimation(appearAnim) }, 80)
                            checkView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })
                } else {
                    checkView.isChecked = false
                    checkView.visibility = View.GONE
                }
            }
        }


    internal class HitomiViewHolder(val binding : ItemHitomiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hitomiItem: HitomiItem) {
            val inflater = LayoutInflater.from(binding.root.context)

            binding.txtTitle.text = hitomiItem.title ?: "제목을 불러올 수 없습니다"
            binding.txtNumber.text = "번호: ${hitomiItem.number}"
            binding.txtWriter.text = "작가: ${hitomiItem.artist?:"불러올 수 없음"}"
            binding.txtSeries.text = "시리즈: ${hitomiItem.series?:"불러올 수 없음"}"
            binding.tags.removeAllViews()
            hitomiItem.tags?.let { tags ->
                for (tag in tags) {
                    val viewTag = TagHitomiBinding.inflate(inflater, binding.root, false)
                    viewTag.rootMain.text = tag
                    binding.tags.addView(viewTag.root)
                }
            }
            val cover = hitomiItem.getFile(1)
            if (cover != null) {
                binding.wrapCoverError.visibility = View.GONE
                binding.imgCover.visibility = View.VISIBLE
                binding.imgCover.setImageURI(Uri.fromFile(cover))
            }
            binding.imgCover.setOnClickListener {
                val intentHitomiView = Intent(binding.root.context, ReadHitomiActivity::class.java)
                intentHitomiView.putExtra("_no", hitomiItem._no)
                binding.root.context.startActivity(intentHitomiView)
            }
        }
    }
    internal class CustomViewHolder(val binding : ItemCustomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(customItem: CustomItem) {
            binding.txtTitle.text = customItem.title ?: "제목을 불러올 수 없습니다"
            binding.txtURL.text = customItem.url
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val viewHolder = when (viewType) {
            Item.ItemType.HITOMI.ordinal -> {
                val binding = ItemHitomiBinding.inflate(inflater, parent, false)
                HitomiViewHolder(binding)
            }
            Item.ItemType.CUSTOM.ordinal -> {
                val binding = ItemCustomBinding.inflate(inflater, parent, false)
                CustomViewHolder(binding)
            }
            else -> null!!
        }
        return viewHolder
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        data class IntersectedViews(
            val rootItem: ViewGroup,
            val expandableView: ViewGroup,
            val checkView: CheckBox,
            val imgArrow: ImageView)
        val (rootItem, expandableView, checkView, imgArrow) = when (holder) {
            is HitomiViewHolder -> {
                val hitomiItem = items[position] as HitomiItem
                val binding = holder.binding
                holder.bind(hitomiItem)

                /* 세부사항 페이지 설정 */

                // viewInfo 크기 측정 후 크기 고정(match_parent or wrap_content => 고정값)
                binding.viewInfo.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                binding.cover.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                binding.viewInfo.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        binding.viewInfo.layoutParams.height = binding.viewInfo.height
                        binding.cover.layoutParams.height = binding.viewInfo.height
                        binding.viewInfo.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
                binding.btnWebview.setOnClickListener {
                    Toast.makeText(binding.root.context, "히토미 보기 페이지로 넘어가야함", Toast.LENGTH_SHORT).show()
                }

                IntersectedViews(binding.rootItem, binding.expandableView, binding.check, binding.imgArrow)
            }
            is CustomViewHolder -> {
                val customItem = items[position] as CustomItem
                val binding = holder.binding
                holder.bind(customItem)

                binding.btnWebview.setOnClickListener {
                    Toast.makeText(binding.root.context, "웹뷰 페이지로 넘어가야함", Toast.LENGTH_SHORT).show()
                }

                IntersectedViews(binding.rootItem, binding.expandableView, binding.check, binding.imgArrow)
            }

            else -> null!!
        }

        // notifyDataSetChanged호출 마다 bind를 하는데, view holder가 재활용 되는 경우가 종종 있어서 초기화를 처음부터 싹 다 해야됨..
        checkViews[position] = checkView
        ToggleAnimation.toggleArrow(imgArrow, false, 0)
        expandableView.visibility = View.VISIBLE // 이거 한줄 추가했더니 wrapViewHeight값 계산이 멀쩡하게 잘된다. 대체왜..?
        expandableView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        expandableView.requestLayout()
        // 항목 꾹 눌러서 선택하기 설정
        rootItem.setOnLongClickListener {
            checkView.isChecked = !checkView.isChecked
            onLongClickItemListener(this)
            true
        }

        // wrapviewHeight = 펼쳐질 항목의 높이 측정 ) 사실 view.measure로 측정했었지만 (이 문제와는 별개로 표시되는 히토미 작품의 정보량이 너무 많아서 높이 300dp를 넘어 넘쳐버리는 일 때문에)
        // xml 레이아웃좀 수정했더니 맛가버림 왜그런지는 5시간정도 고민해보고 검색해보고 했는데 미스터리임.
        // 그런데 화면에 뜨는건 또 정상적으로 떠서 그냥 이거 이용하면 되겠다 싶어서 화면에 출력된 높이 구해오는 방법으로 했음
        var expandableHeight = 800
        expandableView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                expandableHeight = expandableView.height
                expandableView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                expandableView.visibility = View.GONE
            }
        })

        // 항목 펼침/접힘 애니메이션 설정
        var isExpanded = false
        rootItem.setOnClickListener {
            isExpanded = !isExpanded
            ToggleAnimation.toggle(expandableView, expandableHeight, imgArrow, isExpanded)
        }
    }

    fun selectAll(isChecked: Boolean) {
        selectMode = true
        for (checkView in checkViews)
            checkView!!.isChecked = isChecked
    }

    fun getItemChecked(index: Int) = checkViews[index]!!.isChecked
}