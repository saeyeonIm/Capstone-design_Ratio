package com.example.myapplication

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

var moveX: Float = 0f
var moveY: Float = 0f
var distance: Float = 0f

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer


    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer()
        // Render the view only when there is a change in the drawing data
//        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        setRenderer(renderer)

        GLES20.glViewport(0, 0, 300, 300)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        val x: Float = e.x
        val y: Float = e.y

        when (e.action) {
            MotionEvent.ACTION_POINTER_DOWN ->{
                if(e.pointerCount == 2){
                    val x1 = e.getX(0)
                    val y1 = e.getY(0)
                    val x2 = e.getX(1)
                    val y2 = e.getY(1)
                    println("TEST")
                    distance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
                    println("($x1, $y1), ($x2, $y2), $distance")
                }
                requestRender()
            }
            MotionEvent.ACTION_MOVE -> {
                if(e.pointerCount == 2){
                    val x1 = e.getX(0)
                    val y1 = e.getY(0)
                    val x2 = e.getX(1)
                    val y2 = e.getY(1)
                    println("TEST")
                    distance = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
                    println("($x1, $y1), ($x2, $y2), $distance")
                }else{
                    moveX = e.x / 10
                    moveY = e.y / 10
                    println("$moveX, $moveY")
                    requestRender()
                }
                requestRender()
            }
        }
        return true
    }
}