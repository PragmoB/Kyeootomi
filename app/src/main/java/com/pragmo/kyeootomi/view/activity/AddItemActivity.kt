package com.pragmo.kyeootomi.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityAddItemBinding
import com.pragmo.kyeootomi.model.data.Item
import com.pragmo.kyeootomi.view.fragment.AddCustomFragment
import com.pragmo.kyeootomi.view.fragment.AddHitomiFragment
import com.pragmo.kyeootomi.viewmodel.AddItemViewModel

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

        /* MVVM 오늘 처음 공부한거라 잘못쓴걸수도 있지만 뷰 모델 연동 */

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

        // 컨텐츠 제공 업체에 따라 입력 폼 변경
        viewModel.contentsProvider.observe(this) {
            it ?: return@observe
            val transaction = supportFragmentManager.beginTransaction()
            when (it) {
                Item.ItemType.HITOMI -> transaction.replace(R.id.fragmentInput, AddHitomiFragment(viewModel))
                Item.ItemType.CUSTOM-> transaction.replace(R.id.fragmentInput, AddCustomFragment(viewModel))
            }
            transaction.commit()
        }
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
        when (item.itemId) {
            R.id.menuComplete -> {
                if (viewModel.commit()) {
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