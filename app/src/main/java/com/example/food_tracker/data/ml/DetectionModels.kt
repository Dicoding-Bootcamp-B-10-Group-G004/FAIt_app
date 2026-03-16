package com.example.food_tracker.data.ml

import android.graphics.Bitmap
import android.graphics.RectF

data class DetectionResult(
    val detections: List<Detection>,
    val inferenceTime: Long,
    val inputImageHeight: Int,
    val inputImageWidth: Int,
)

data class Detection(
    val label: String,
    val boundingBox: RectF,
    val score: Float
)

data class SnappedResult(
    val bitmap: Bitmap,
    val result: DetectionResult
)
