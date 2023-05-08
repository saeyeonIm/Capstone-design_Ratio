package com.example.indoornavigationapp.view.opengl

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MyGLSurfaceView
import org.json.JSONArray
import org.json.JSONObject

var roadsArray: JSONArray = JSONArray();
var octagonObject: JSONObject = JSONObject()
var soccerFieldObject: JSONObject = JSONObject()

class OpenGLActivity : Activity() {

    private lateinit var gLView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Json 파일 읽는 부분
        val jsonString = assets.open("jsons/coordinate.json").bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)
        roadsArray = json.getJSONArray("Roads")
        octagonObject = json.getJSONObject("Octagon")
        soccerFieldObject = json.getJSONObject("soccer_field")

        gLView = MyGLSurfaceView(this)
        setContentView(gLView)
    }
}