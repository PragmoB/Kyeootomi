package com.pragmo.kyeootomi.view.fragment.item.add

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.pragmo.kyeootomi.R
import com.pragmo.kyeootomi.databinding.FragmentAddHitomiBinding
import com.pragmo.kyeootomi.viewmodel.item.add.AddHitomiViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateHitomiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddHitomiFragment() : Fragment() {

    private lateinit var binding : FragmentAddHitomiBinding
    private val viewModel: AddHitomiViewModel by activityViewModels()

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_hitomi, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.titleOpt.observe(viewLifecycleOwner) {
            if (it == R.id.radioSetCustomTitle)
                binding.editTitle.visibility = View.VISIBLE
            else
                binding.editTitle.visibility = View.GONE
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
            AddHitomiFragment().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                }
            }
    }
}