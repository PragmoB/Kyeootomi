package com.pragmo.kyeootomi.view.fragment.item.update

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentUpdateCustomBinding
import com.pragmo.kyeootomi.viewmodel.item.update.UpdateCustomViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateCustomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateCustomFragment : Fragment() {

    private lateinit var binding: FragmentUpdateCustomBinding
    private val viewModel: UpdateCustomViewModel by activityViewModels()
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_custom, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            UpdateCustomFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}