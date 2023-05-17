package com.example.indoornavigationapp.view.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// number of coordinates per vertex in this array
// x, y, z -> 3
const val COORDS_PER_VERTEX = 3

class Polygon(coordinates: FloatArray, color: FloatArray, drawOrder: ShortArray) {

    private var color: FloatArray? = null
    private var coordinates: FloatArray? = null
    private var drawOrder: ShortArray? = null
    private var mProgram: Int
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = coordinates.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex


    // 쉐이더 코드 - 그래픽 하드웨어에서 실행되는 작은 프로그램으로 그래픽의 모양과 외관을 제어하는 데 사용

    //정점의 위치를 계산
    private val vertexShaderCode =
    // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    //픽셀의 색상을 계산
    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    // 정점 정보를 담고 있는 버퍼. openGL은 최적화를 위해 ByteBuffer를 사용함.
    private val vertexBuffer: FloatBuffer =
        // 버퍼의 크기 할당
        ByteBuffer.allocateDirect(coordinates.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(coordinates)
                position(0)
            }
        }

    // 그리기 목록에 대한 바이트 버퍼 초기화
    private val drawListBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    // 뷰 변환에 액세스하고 설정하는 데 사용
    private var vPMatrixHandle: Int = 0

    init {
        this.color = color
        this.coordinates = coordinates
        this.drawOrder = drawOrder

        // 쉐이더 생성
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->

            // 지정된 쉐이더에 쉐이더 코드 추가하고 컴파일
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        // 버텍스 셰이더의 vPosition 멤버에 대한 핸들 가져오기
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {

            // 쉐이더 관련 속성을 활성화 (랜더링 가능하도록)
            GLES20.glEnableVertexAttribArray(it)

            // 좌표 데이터 준비
            // glVertexAttribPointer 함수는 OpenGL ES에서 특정 속성에 대한 데이터 형식과 메모리 위치를 설정하는 데 사용
            GLES20.glVertexAttribPointer(
                it, //속성의 위치
                COORDS_PER_VERTEX, //속성당 구성 요소 수
                GLES20.GL_FLOAT, //각 구성 요소의 데이터 유형
                false, //데이터가 정규화되었는지 여부
                vertexStride, //속성 데이터의 버퍼 오프셋
                vertexBuffer //속성 데이터가 포함된 버퍼 개체의 참조
            )

            // fragment shader의 vColor 멤버에 대한 핸들 가져오기
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->

                // 그리기 색상 설정
                GLES20.glUniform4fv(colorHandle, 1, color, 0)
            }

            // // 형상의 변환 행렬에 대한 핸들 획득
            vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

            // 투영 및 뷰 변환을 셰이더로 전달
            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

            // Draw
            if (drawOrder != null) {
                GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    drawOrder!!.size,
                    GLES20.GL_UNSIGNED_SHORT,
                    drawListBuffer
                )
            }else{
                // 삼각형 그리기
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
            }

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }
}