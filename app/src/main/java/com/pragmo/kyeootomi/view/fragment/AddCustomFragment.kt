package com.pragmo.kyeootomi.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentAddCustomBinding
import com.pragmo.kyeootomi.viewmodel.AddItemViewModel

class AddCustomFragment(private val viewModel: AddItemViewModel) : Fragment() {

    private lateinit var binding : FragmentAddCustomBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_custom, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}