package com.pragmo.kyeootomi.view.activity.item

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityAddItemBinding
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.view.fragment.item.add.AddCustomFragment
import com.pragmo.kyeootomi.view.fragment.item.add.AddHitomiFragment
import com.pragmo.kyeootomi.viewmodel.item.add.AddCustomViewModel
import com.pragmo.kyeootomi.viewmodel.item.add.AddHitomiViewModel
import com.pragmo.kyeootomi.viewmodel.item.add.AddItemViewModel

class AddItemActivity : AppCompatActivity() {

    private lateinit var viewModel : AddItemViewModel
    private lateinit var binding : ActivityAddItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val numCollection = intent.getIntExtra("numCollection", 0)
        viewModel = ViewModelProvider(this)[AddItemViewModel::class.java]
        viewModel.setCollection(if (numCollection == 0) null else numCollection)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_item)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "컬렉션 작품 추가"


        // 컨텐츠 제공 업체 폼 설정
        val itemTypeValues = Item.ItemType.values()
        val nameContentsProviders = Array(itemTypeValues.size) { "" }
        itemTypeValues.forEachIndexed { index, itemType ->
            nameContentsProviders[index] = if (itemType.domain.isEmpty())
                itemType.otherName
            else
                "${itemType.otherName}(${itemType.domain})"
        }
        binding.spinContentsProvider.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, nameContentsProviders
        )

        /* MVVM 오늘 처음 공부한거라 잘못쓴걸수도 있지만 뷰 모델 연동 */

        // 컨텐츠 제공 업체에 따라 입력 폼 변경
        viewModel.contentsProviderOrdinal.observe(this) {
            it ?: return@observe
            val contentsProvider = Item.ItemType.values().getOrNull(it) ?: return@observe
            viewModel = when (contentsProvider) {
                Item.ItemType.HITOMI -> ViewModelProvider(this)[AddHitomiViewModel::class.java]
                Item.ItemType.CUSTOM -> ViewModelProvider(this)[AddCustomViewModel::class.java]
            }.apply {
                setCollection(if (numCollection == 0) null else numCollection)
                contentsProviderOrdinal.value = contentsProvider.ordinal
            }
            val fragment = when (contentsProvider) {
                Item.ItemType.HITOMI -> AddHitomiFragment.newInstance()
                Item.ItemType.CUSTOM-> AddCustomFragment.newInstance()
            }

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentInput, fragment)
            transaction.commit()
        }

        // 일단은 히토미로 선택
        viewModel.contentsProviderOrdinal.value = Item.ItemType.HITOMI.ordinal
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val onCommitComplete: (Boolean) -> Unit = {
            if (it)
                Toast.makeText(this, "추가되었습니다", Toast.LENGTH_SHORT).show()
        }
        when (item.itemId) {
            R.id.menuComplete -> {
                if (viewModel.commit(onCommitComplete)) {
                    Toast.makeText(this, "추가 중 입니다 잠시만 기다려주세요", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else
                    Toast.makeText(this, "남아있는 입력 폼을 작성해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}