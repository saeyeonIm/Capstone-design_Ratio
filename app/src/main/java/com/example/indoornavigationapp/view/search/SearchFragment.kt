package com.example.indoornavigationapp.view.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.indoornavigationapp.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    companion object {
        const val DEFAULT_DESTINATION_ID = -1
    }

    private var binding: FragmentSearchBinding? = null
    private var destinationId: Int = DEFAULT_DESTINATION_ID


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater)

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}