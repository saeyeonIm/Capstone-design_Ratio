package com.example.indoornavigationapp.view.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix

import com.example.indoornavigationapp.mapComponentList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyGLRenderer : GLSurfaceView.Renderer {

    // vPMatrix는 "Model View Projection Matrix"의 약어입니다
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    var polygonList = ArrayList<Polygon>()

    // 환경 설정을 위해 초기에 한 번 호출
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        for (mapComponent in mapComponentList) {
            val polygonCoords = mapComponent.coordinates
                .flatMap { it.map{ it / 1000f} }.toFloatArray()
            val color = mapComponent.color
            val drawOrder = mapComponent.drawOrder
            polygonList.add(Polygon(polygonCoords, color, drawOrder))
        }
    }

    // 장치의 화면 방향 변실 시 호출
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // 투영 행렬 구성, 투영 행렬은 3차원 공간의 물체를 2차원에 투영할 때 사용된다.
        // x-왼쪽, 오른쪽  y-아래, 위 z-앞, 뒤
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 300000f)

    }

    // 뷰를 다시 그릴 때 호출
    override fun onDrawFrame(gl: GL10?) {
        // Redraw background color
        // 색상 버퍼를 비움
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 카메라 위치 설정(보기 매트릭스)
        // 이 행렬은 카메라가 (0, 0, 3)에 있고, 보고 있는 지점이 (0, 0, 0)에 있고, 위쪽이 (0, 1, 0)에 있는 것처럼 3차원 공간의 물체를 봅니다.
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.setLookAtM(viewMatrix, 0, realX, realY , realZ, realX, realY, 0f, 0f, 1.0f, 0.0f)
        // 투영 및 뷰 변환 계산
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // 지도 그리는 부분
        for(polygon in polygonList){
            polygon.draw(vPMatrix)
        }

        GLES20.glFlush()
    }
}