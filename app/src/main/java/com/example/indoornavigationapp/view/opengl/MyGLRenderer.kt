package com.example.indoornavigationapp.view.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.indoornavigationapp.octagonObject
import com.example.indoornavigationapp.roadsArray
import com.example.indoornavigationapp.soccerFieldObject
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList

class MyGLRenderer : GLSurfaceView.Renderer {


    private lateinit var ground: Square
    private lateinit var mOctagon: Octagon

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val loadList = ArrayList<Square>()

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        for (i in 0 until roadsArray.length()){
            val roadObject = roadsArray.getJSONObject(i)
            val point1 = roadObject.getJSONArray("point1")
            val point2 = roadObject.getJSONArray("point2")
            val point3 = roadObject.getJSONArray("point3")
            val point4 = roadObject.getJSONArray("point4")
            val loadElement = floatArrayOf(
                (point1.getDouble(0) / 1000).toFloat(), (point1.getDouble(1) / 1000).toFloat(), (point1.getDouble(2) / 1000).toFloat(),
                (point2.getDouble(0) / 1000).toFloat(), (point2.getDouble(1) / 1000).toFloat(), (point2.getDouble(2) / 1000).toFloat(),
                (point3.getDouble(0) / 1000).toFloat(), (point3.getDouble(1) / 1000).toFloat(), (point3.getDouble(2) / 1000).toFloat(),
                (point4.getDouble(0) / 1000).toFloat(), (point4.getDouble(1) / 1000).toFloat(), (point4.getDouble(2) / 1000).toFloat()
            )
            loadList.add(Square(loadElement))
        }

        println(octagonObject.getJSONArray("point1").getDouble(0) / 1000)
        val octagonElement = floatArrayOf(
            (octagonObject.getJSONArray("point1").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point1").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point1").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point2").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point2").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point2").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point3").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point3").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point3").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point4").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point4").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point4").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point5").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point5").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point5").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point6").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point6").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point6").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point7").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point7").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point7").getDouble(2)/1000).toFloat(),
            (octagonObject.getJSONArray("point8").getDouble(0)/1000).toFloat(), (octagonObject.getJSONArray("point8").getDouble(1)/1000).toFloat(), (octagonObject.getJSONArray("point8").getDouble(2)/1000).toFloat(),
        )
        mOctagon = Octagon(octagonElement)

        val soccerFieldElement = floatArrayOf(
            (soccerFieldObject.getJSONArray("point1").getDouble(0)/1000).toFloat(), (soccerFieldObject.getJSONArray("point1").getDouble(1)/1000).toFloat(), (soccerFieldObject.getJSONArray("point1").getDouble(2)/1000).toFloat(),
            (soccerFieldObject.getJSONArray("point2").getDouble(0)/1000).toFloat(), (soccerFieldObject.getJSONArray("point2").getDouble(1)/1000).toFloat(), (soccerFieldObject.getJSONArray("point2").getDouble(2)/1000).toFloat(),
            (soccerFieldObject.getJSONArray("point3").getDouble(0)/1000).toFloat(), (soccerFieldObject.getJSONArray("point3").getDouble(1)/1000).toFloat(), (soccerFieldObject.getJSONArray("point3").getDouble(2)/1000).toFloat(),
            (soccerFieldObject.getJSONArray("point4").getDouble(0)/1000).toFloat(), (soccerFieldObject.getJSONArray("point4").getDouble(1)/1000).toFloat(), (soccerFieldObject.getJSONArray("point4").getDouble(2)/1000).toFloat(),
        )
        ground = Square(soccerFieldElement)
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        val width = p1
        val height = p2
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / width.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 300000f)

    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 225.057f + moveX, 159.636f+moveY , 1000f - distance, 225.057f + moveX, 159.636f+ moveY, 0f, 0f, 1.0f, 0.0f)
//        Matrix.setLookAtM(viewMatrix, 0, 225.057f, 159.636f , 1000f, 225.057f, 159.636f, 0f, 0f, 1.0f, 0.0f)


        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        var octagonColor = floatArrayOf(0.9f, 0.9f, 0.9f, 1.0f)
        mOctagon.draw(vPMatrix,octagonColor)
        var groundColor = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
        ground.draw(vPMatrix, groundColor)
        var loadColor = floatArrayOf(0.6f, 0.6f, 0.6f, 1.0f)
        for(load in loadList){
            load.draw(vPMatrix, loadColor)
        }
        GLES20.glFlush()
    }
}