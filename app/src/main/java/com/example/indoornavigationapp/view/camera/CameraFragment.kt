package com.example.indoornavigationapp.view.camera

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import apriltag.ApriltagDetection
import apriltag.CameraPosEstimation
import apriltag.OpenCVNative
import com.example.indoornavigationapp.R
import com.example.indoornavigationapp.databinding.FragmentCameraBinding
import com.example.indoornavigationapp.listener.TagDetectionListener
import com.example.indoornavigationapp.view.ApriltagCamera2View
import com.example.indoornavigationapp.view.CameraBridgeViewBaseImpl
import com.example.indoornavigationapp.view.opengl.MyGLSurfaceView
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import kotlin.time.ExperimentalTime

class CameraFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback,
    CameraBridgeViewBaseImpl.MyCvCameraViewListener2, TagDetectionListener {

    var binding: FragmentCameraBinding? = null

    private var glView: MyGLSurfaceView? = null
    private var mOpenCvCameraView: ApriltagCamera2View? = null
    private val permissionList = Manifest.permission.CAMERA
    private var mSize: Size = Size(-1, -1)
    private lateinit var cameraMatrixData: DoubleArray
    private lateinit var matInput: Mat
    private var aprilDetections: ArrayList<ApriltagDetection>? = null
    private var isCameraMatInit: Boolean = false
    private var posEstimateResults = ArrayList<CameraPosEstimation>()

    private val viewModel: CameraViewModel by viewModels()

    init {
        arrowShapes[0] = defaultCoords
        arrowShapes[1] = arrowLeftCoords
        arrowShapes[2] = arrowRightCoords
        arrowShapes[3] = arrowForwardCoords
        arrowShapes[4] = arrowBackwardCoords
    }


    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        when (it) {
            true -> {
                onCameraPermissionGranted()
            }
            false -> {
                showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.")
            }
        }
    }
    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    mOpenCvCameraView?.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialogForPermission(msg: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("알림")
        builder.setMessage(msg)
        builder.setCancelable(false)
        builder.setPositiveButton(
            "예"
        ) { _, _ ->
            findNavController().navigate(R.id.action_cameraFragment_to_entryFragment)
        }
        builder.create().show()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater)

        mOpenCvCameraView = binding?.activitySurfaceView?.apply {
            this.setOnDetectionListener(this@CameraFragment)
            this.visibility = SurfaceView.VISIBLE
            this.setCvCameraViewListener(this@CameraFragment)
            this.setCameraIndex(0) // front-camera(1),  back-camera(0)
        }

        viewModel.estimatedPos.observe(viewLifecycleOwner){
            binding?.absoluteCoorTxt?.text = "Tag Id : ${viewModel.relativePos[3].toInt()}\n절대좌표:\nx : ${it.first}\ny : ${it.second}"
            binding?.relativeCoorTxt?.text = "상대좌표:\nx : ${viewModel.relativePos[0]}\ny : ${viewModel.relativePos[1]}\nz : ${viewModel.relativePos[2]}"
        }

        glView = MyGLSurfaceView(requireContext())

        binding?.openglContainer?.addView(glView)

            return binding?.root
    }


    //여기서부턴 퍼미션 관련 메소드
    private fun getCameraViewList(): List<CameraBridgeViewBaseImpl>? {

        mOpenCvCameraView?.let {
            return listOf<ApriltagCamera2View>(it)
        }
        return null
    }

    private fun onCameraPermissionGranted() {
        val cameraViews = getCameraViewList() ?: return
        for (cameraBridgeViewBase in cameraViews) {
            cameraBridgeViewBase.setCameraPermissionGranted()
        }
    }

    override fun onStart() {
        super.onStart()
        var havePermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(permissionList)
                havePermission = false
            }
        }
        if (havePermission) {
            onCameraPermissionGranted()
        }
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()

    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, mLoaderCallback)
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onTagDetect(aprilDetection: ArrayList<ApriltagDetection>) {
        this.aprilDetections = aprilDetection
        viewModel.onTagDetect(aprilDetection)
    }

    override fun onCameraViewStarted(width: Int, height: Int, focalLength: Double) {
        mSize = Size(width, height)
        cameraMatrixData = doubleArrayOf(
            focalLength, 0.0, width / 2.0,
            0.0, focalLength, height / 2.0,
            0.0, 0.0, 1.0
        )
    }

    override fun onCameraViewStopped() {
    }

    @OptIn(ExperimentalTime::class)
    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        matInput = inputFrame.rgba()

        aprilDetections?.let {
            if (!isCameraMatInit){
                val focalLength = OpenCVNative.find_camera_focal_center(it[0].p, intArrayOf(mSize.width, mSize.height))
                cameraMatrixData[2] = focalLength[0] // Cx
                cameraMatrixData[5] = focalLength[1] // Cy
                isCameraMatInit = true
            }
            for (detection in it) {
                val posEstiResult = OpenCVNative.draw_and_estimate_camera_position(
                    matInput.nativeObjAddr,
                    cameraMatrixData,
                    detection.p,
                    arrowShapes[0]
                )

                posEstiResult.id = detection.id
                posEstimateResults.add(posEstiResult)
            }
            viewModel.onCameraFrame(posEstimateResults)
            posEstimateResults.clear()
            aprilDetections = null
        }

        return matInput
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
        aprilDetections = null
        binding = null
    }

    companion object {

        private const val TAG = "CameraFragment"

        val defaultCoords = arrayOf(
            0.5, 0.5, 0.0,
            0.5, -0.5, 0.0,
            -0.5, -0.5, 0.0,
            -0.5, 0.5, 0.0
        ).toDoubleArray()

        val arrowRightCoords = arrayOf(
            2.0, 1.0, 0.0,
            2.0, 1.5, 0.0,
            3.0, 0.0, 0.0,
            2.0, -1.5, 0.0,
            2.0, -1.0, 0.0,
            -2.0, -1.0, 0.0,
            -2.0, 1.0, 0.0
        ).toDoubleArray()

        val arrowLeftCoords = arrayOf(
            -2.0, 1.0, 0.0,
            -2.0, 1.5, 0.0,
            -3.0, 0.0, 0.0,
            -2.0, -1.5, 0.0,
            -2.0, -1.0, 0.0,
            2.0, -1.0, 0.0,
            2.0, 1.0, 0.0
        ).toDoubleArray()

        val arrowForwardCoords = arrayOf(
            1.0, 0.0, -5.0,
            1.0, 0.0, -2.0,
            1.5, 0.0, -2.0,
            0.0, 0.0, -0.5,
            -1.5, 0.0, -2.0,
            -1.0, 0.0, -2.0,
            -1.0, 0.0, -5.0
        ).toDoubleArray()

        val arrowBackwardCoords = arrayOf(
            -1.2, 0.0, -0.3,
            -1.2, 0.0, -4.3,
            -1.7, 0.0, -4.3,
            0.0, 0.0, -5.3,
            1.7, 0.0, -4.3,
            1.2, 0.0, -4.3,
            1.2, 0.0, -0.3
        ).toDoubleArray()

        /** 화살표들의 배열입니다*/
        var arrowShapes = Array<DoubleArray>(5) { doubleArrayOf() }
    }

}