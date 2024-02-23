package com.pragmo.kyeootomi.view.activity

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import com.google.android.material.navigation.NavigationView
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        /* 툴바 설정 */

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle = ActionBarDrawerToggle(this, binding.drawer,
            R.string.drawer_opened,
            R.string.drawer_closed
        )
        toggle.syncState()

        /* 이벤트 콜백 설정 */

        binding.btnAddItem.setOnClickListener {
            val intentAddItem = Intent(this, AddItemActivity::class.java)
            startActivity(intentAddItem)
        }
        binding.naviView.setNavigationItemSelectedListener(this)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAddCollection -> {
                val intentAddCollection = Intent(this, AddCollectionActivity::class.java)
                startActivity(intentAddCollection)
            }
        }
        return true
    }
}