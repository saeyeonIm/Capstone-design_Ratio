package com.example.indoornavigationapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import apriltag.ApriltagNative
import com.example.indoornavigationapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val navController = binding.fragmentContainerView.getFragment<NavHostFragment>().navController
        setupActionBarWithNavController(navController)

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        ApriltagNative.apriltag_native_init()
        ApriltagNative.apriltag_init("tagStandard41h12", 2, 4.0, 0.0, 1)

    }

    companion object {
        // Used to load the library on application startup.
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("opencv_native_lib")
            System.loadLibrary("apriltag_native_lib")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = binding.fragmentContainerView.getFragment<NavHostFragment>().navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}