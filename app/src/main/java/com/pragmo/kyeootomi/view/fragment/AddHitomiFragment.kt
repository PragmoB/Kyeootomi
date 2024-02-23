package com.pragmo.kyeootomi.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentAddHitomiBinding
import com.pragmo.kyeootomi.viewmodel.AddItemViewModel

class AddHitomiFragment(private val viewModel: AddItemViewModel) : Fragment() {

    private lateinit var binding : FragmentAddHitomiBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddHitomiBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.useTitle.observe(viewLifecycleOwner) {
            if (it?:false)
                binding.editTitle.visibility = View.VISIBLE
            else
                binding.editTitle.visibility = View.GONE
        }
        return binding.root
    }
}