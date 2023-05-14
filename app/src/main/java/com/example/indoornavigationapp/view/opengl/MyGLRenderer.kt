package com.example.indoornavigationapp.view.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    // 환경 설정을 위해 초기에 한 번 호출
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    // 장치의 화면 방향 변실 시 호출
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

    }

    // 뷰를 다시 그릴 때 호출
    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color
        // 색상 버퍼를 비움
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}