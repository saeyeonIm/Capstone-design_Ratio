package com.example.indoornavigationapp.view.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.sqrt

// 초기 좌표 지점
var realX: Float = 225.057f
var realY: Float = 159.636f
var realZ: Float = 100f
val zoom: Float = 10f
val moveSpeed: Float = 50f
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
    var prevDistance = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when(event.action){
            MotionEvent.ACTION_DOWN ->{
                prevX = x
                prevY = y
            }
            MotionEvent.ACTION_MOVE ->{
                if (event.pointerCount == 2) {
                    val x1 = event.getX(0)
                    val y1 = event.getY(0)
                    val x2 = event.getX(1)
                    val y2 = event.getY(1)

                    // 현재 거리와 전의 거리의 차를 갖고 확대하는 지 축소하는 지 계산
                    val curDistance = getDistance(x1, y1, x2, y2)

                    if(prevDistance > curDistance ){
                        realZ += zoom
                    }else if (prevDistance < curDistance && realZ > zoom){
                        realZ -= zoom
                    }

                    prevDistance = curDistance
                }else{
                    val moveX = prevX - x
                    val moveY =  y - prevY
                    prevX = x
                    prevY = y
                    realX += moveX / moveSpeed
                    realY += moveY / moveSpeed
                }
            }
        }
        return true
    }

    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float{
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }
}