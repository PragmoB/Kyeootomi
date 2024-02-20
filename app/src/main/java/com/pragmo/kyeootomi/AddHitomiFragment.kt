package com.pragmo.kyeootomi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.pragmo.kyeootomi.databinding.FragmentAddHitomiBinding

class AddHitomiFragment : Fragment() {

    lateinit var binding : FragmentAddHitomiBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddHitomiBinding.inflate(inflater)
        binding.radioTitleOption.setOnCheckedChangeListener(objTitleCheckedChangeCallback)
        return binding.root
    }

    /* 제목 설정 옵션에 따라 제목 입력 창을 숨김 */

    private val objTitleCheckedChangeCallback =
        RadioGroup.OnCheckedChangeListener { group, checkedId ->
        when (checkedId) {
            R.id.radioTitleAuto -> binding.editTitle.visibility = View.GONE
            R.id.radioTitleCustom -> binding.editTitle.visibility = View.VISIBLE
        }
    }
}