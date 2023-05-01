package com.example.indoornavigationapp.listener

import apriltag.ApriltagDetection

interface TagDetectionListener {
    fun onTagDetect(aprilDetection: ArrayList<ApriltagDetection>)
}