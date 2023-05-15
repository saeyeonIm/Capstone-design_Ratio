package com.example.indoornavigationapp.view.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer

    init {

        // 버전 설정
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer()
        setRenderer(renderer)

    }
}