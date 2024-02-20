package com.pragmo.kyeootomi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import com.pragmo.kyeootomi.databinding.ActivityAddItemBinding

class AddItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "컬렉션 작품 추가"

        /* 이벤트 콜백 설정 */

        binding.spinContentsProvider.onItemSelectedListener = objSpinContentsProviderCallback
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val objSpinContentsProviderCallback = object: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            /* 컨텐츠 제공업체에 따라 입력 폼 변경 */

            val transaction = supportFragmentManager.beginTransaction()
            val contentsProviders = resources.getStringArray(R.array.contents_providers)
            when (contentsProviders[position]) {
                "hitomi" -> transaction.replace(R.id.fragmentInput, AddHitomiFragment())
                "custom" -> transaction.replace(R.id.fragmentInput, AddCustomFragment())
            }
            transaction.commit()
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }
}