package com.pragmo.kyeootomi.view.fragment.item.update

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentUpdateHitomiBinding
import com.pragmo.kyeootomi.viewmodel.item.update.UpdateHitomiViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateHitomiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateHitomiFragment : Fragment() {
    private lateinit var binding: FragmentUpdateHitomiBinding

    private val viewModel: UpdateHitomiViewModel by activityViewModels()
    // TODO: Rename and change types of parameters
   //private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_hitomi, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.titleOpt.observe(viewLifecycleOwner) {
            when (it) {
                R.id.radioSetAutoTitle -> {
                    viewModel.reloadInfo.value = true
                    binding.checkReloadInfo.isEnabled = false
                    binding.editTitle.visibility = View.GONE
                }
                else -> {
                    binding.checkReloadInfo.isEnabled = true
                    binding.editTitle.visibility = View.VISIBLE
                }
            }
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdateHitomiFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(//param1: String
        ) =
            UpdateHitomiFragment().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                }
            }
    }
}