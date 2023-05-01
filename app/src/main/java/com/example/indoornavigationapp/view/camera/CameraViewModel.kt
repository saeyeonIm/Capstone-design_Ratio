package com.example.indoornavigationapp.view.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apriltag.ApriltagDetection
import apriltag.CameraPosEstimation
import com.example.indoornavigationapp.model.Tag
import com.example.indoornavigationapp.repository.MemoryTagRepository
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class CameraViewModel : ViewModel() {

    private val repository: MemoryTagRepository = MemoryTagRepository()

    private val _estimatedPos = MutableLiveData<Pair<Double, Double>>(Pair(0.0, 0.0))
    val estimatedPos: LiveData<Pair<Double, Double>>
        get() = _estimatedPos

    var relativePos: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0, 0.0)

    fun onCameraFrame(posEstimations: ArrayList<CameraPosEstimation>) {
        var avgX = 0.0
        var avgY = 0.0
        var count = 0

        for (est in posEstimations) {
            val absPos = estimateAbsCamPos(est)
            if (absPos == null) {
                count++
                continue
            }
            avgX += absPos.first
            avgY += absPos.second

        }
        relativePos[0] = posEstimations[0].relativeCameraPos[0]
        relativePos[1] = posEstimations[0].relativeCameraPos[1]
        relativePos[2] = posEstimations[0].relativeCameraPos[2]
        relativePos[3] = posEstimations[0].id.toDouble()
        _estimatedPos.postValue(
            Pair(
                avgX / (posEstimations.size - count),
                avgY / (posEstimations.size - count)
            )
        )
    }

    fun onTagDetect(detections: ArrayList<ApriltagDetection>) {
    }

    /**
     * opencv가 구한 카메라 추정 위치를 이용해 평면 좌표계상 위치를 추정합니다.
     */
    private fun estimateAbsCamPos(posEstimation: CameraPosEstimation): Pair<Double, Double>? {
        val x = posEstimation.relativeCameraPos[0]
        val z = posEstimation.relativeCameraPos[2]
        repository.findTagById(posEstimation.id)?.let {
            val detectedTag = it
            val distance = hypot(x, z)
            val theta = atan(z / x) - detectedTag.rot

            val cos = if (theta > 0) cos(theta) else -cos(theta)
            val sin = if (theta > 0) sin(theta) else -sin(theta)
            val camPosX = detectedTag.x - distance * cos
            val camPosY = detectedTag.y + distance * sin
            return Pair(camPosX, camPosY)
        }
        return null
    }
}