package com.example.indoornavigationapp.view.entry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.indoornavigationapp.R
import com.example.indoornavigationapp.databinding.FragmentEntryBinding


class EntryFragment : Fragment() {
    var binding: FragmentEntryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEntryBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding?.btnCamera?.setOnClickListener {
            findNavController().navigate(R.id.action_entryFragment_to_cameraFragment)
        }

        binding?.btnSearch?.setOnClickListener {
            findNavController().navigate(R.id.action_entryFragment_to_searchFragment)
        }


        binding?.btnSettings?.setOnClickListener {
            findNavController().navigate(R.id.action_entryFragment_to_settingsFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}