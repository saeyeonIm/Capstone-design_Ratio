package com.example.indoornavigationapp.view.opengl

import android.content.ContentValues.TAG
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import kotlin.math.sqrt

var realX: Float = 225.057f
var realY: Float = 159.636f
var realZ: Float = 100f
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer

    init {

        // 버전 설정
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer()
        setRenderer(renderer)

    }

    var prevX = 0f
    var prevY = 0f
    var prevZ = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when(event.action){


            MotionEvent.ACTION_DOWN ->{
                println("ACTION_DOWN ${event.pointerCount}")
                prevX = x
                prevY = y
            }

            MotionEvent.ACTION_MOVE ->{

                if (event.pointerCount == 2) {
                    val x1 = event.getX(0)
                    val y1 = event.getY(0)
                    val x2 = event.getX(1)
                    val y2 = event.getY(1)

                    // 두 터치 사이의 거리 계산
                    // 현재 거리와 전의 거리의 차를 갖고 확대하는 지 축소하는 지 계산
                    val moveZ = prevZ - sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
                    prevZ = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))

                    if(moveZ > 0 ){
                        realZ += 10
                    }else if (moveZ < 0 && realZ - 10 > 0){
                        realZ -= 10
                    }


                }else{
                    val moveX = prevX - x
                    val moveY =  y - prevY
                    prevX = x
                    prevY = y
                    realX += moveX/50
                    realY += moveY/50
                }
            }
        }
        return true
    }
}