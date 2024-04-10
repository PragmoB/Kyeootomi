package com.pragmo.kyeootomi.view.activity

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.pragmo.kyeootomi.databinding.ActivitySwipeLockBinding
import com.sothree.slidinguppanel.SlidingUpPanelLayout


class SwipeLockActivity : BaseActivity() {
    private lateinit var binding: ActivitySwipeLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwipeLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        binding.slidingUpPanel.addPanelSlideListener(PanelEventListener())

        binding.slideLayout.setOnClickListener {
            binding.slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        binding.background.setOnClickListener {
            binding.slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    inner class PanelEventListener: SlidingUpPanelLayout.PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
        }

        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?
        ) {
            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                finish()
        }
    }
}