package com.example.indoornavigationapp.view.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import apriltag.ApriltagNative
import com.example.indoornavigationapp.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null

    private val decimates: Array<String> = arrayOf("1.0", "1.5", "2.0", "3.0", "4.0")
    private val threads: Array<String> = arrayOf("1", "2", "4", "6", "8")
    private val tagFamilies: Array<String> = arrayOf("tag16h5" ,"tag25h9","tag36h10" ,"tag36h11",  "tagStandard41h12", "tagStandard52h13")

    /** default value */
    var decimateFactor = 4.0
    var tagFamily = "tagStandard52h13"
    var thread = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)
        // Inflate the layout for this fragment
        binding?.btnDecimation?.setOnClickListener {
            val builder =
                AlertDialog.Builder(this.activity)
            builder.setTitle("Select Station").setItems(
                decimates
            ) { _, which ->
                Toast.makeText(
                    this.activity,
                    "decimateFactor로 ${decimates[which]}이 선택되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                decimateFactor = decimates[which].toDouble()
                ApriltagNative.apriltag_init(tagFamily, 2, decimateFactor, 0.0, thread)
            }.show()


        }

        binding?.btnThreads?.setOnClickListener {
            val builder =
                AlertDialog.Builder(this.activity)
            builder.setTitle("Select Station").setItems(
                threads
            ) { _, which ->
                Toast.makeText(
                    this.activity,
                    "${threads[which]}개 thread가 사용됩니다.",
                    Toast.LENGTH_SHORT
                ).show()
                thread = threads[which].toInt()
                ApriltagNative.apriltag_init(tagFamily, 2, decimateFactor, 0.0, thread)
            }.show()


        }
        binding?.btnTagFamily?.setOnClickListener {
            val builder =
                AlertDialog.Builder(this.activity)
            builder.setTitle("Select Station").setItems(
                tagFamilies
            ) { _, which ->
                Toast.makeText(
                    this.activity,
                    "${tagFamilies[which]} family가 선택되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                tagFamily = tagFamilies[which]
                ApriltagNative.apriltag_init(tagFamily, 2, decimateFactor, 0.0, thread)
            }.show()


        }
        return binding?.root
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}