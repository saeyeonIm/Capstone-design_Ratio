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
import com.example.indoornavigationapp.view.opengl.MapComponent
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.lang.Exception

//var mapData: JSONArray = JSONArray()
var mapComponentList: List<MapComponent> = emptyList()

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

        // 지도 구성 요소 리스트
        mapComponentList = getMapComponentListFromJson("jsons/coordinate.json")
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

    // Json 파일 읽어서 객체화하는 코드
    private fun getMapComponentListFromJson(path: String): List<MapComponent>{
        val list = mutableListOf<MapComponent>()
        val gson = Gson()
        try {
            val inputStream = assets.open(path)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)

            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("data")

            for (index in 0 until jsonArray.length()) {
                val mapComponent =
                    gson.fromJson(jsonArray.get(index).toString(), MapComponent::class.java)
                list.add(mapComponent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}


