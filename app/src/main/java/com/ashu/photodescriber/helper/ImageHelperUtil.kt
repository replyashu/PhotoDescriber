package com.ashu.photodescriber.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.ashu.photodescriber.repository.db.ImageRepository
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object ImageHelperUtil {

    fun containsFace(bitmap: Bitmap, faceDetector: FaceDetector): Boolean {

        val inputImage = BitmapImageBuilder(bitmap).build()
        val results = faceDetector.detect(inputImage, ImageProcessingOptions.builder().build())

        return results.detections().isNotEmpty()
    }

    suspend fun detectAndSaveImage(imgPath: String, image: Bitmap, faceDetector: FaceDetector,
                           context: Context, repository: ImageRepository) {

        val mpImage = BitmapImageBuilder(image).build()
        val result = faceDetector.detect(mpImage)

        val mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        // Draw bounding boxes
        result.detections().forEach { detection ->
            val boundingBox = detection.boundingBox()

            // Convert normalized bounding box to pixel coordinates
            val left = boundingBox.left
            val top = boundingBox.top
            val right = boundingBox.right
            val bottom = boundingBox.bottom

            canvas.drawRect(left, top, right, bottom, paint)
        }

        val uri = mutableBitmap.rotatedBy(270f).toUriDownload(context)
        withContext(Dispatchers.IO) {
            repository.insert(imgPath, uri.toString())
        }
    }

    fun detectFaces(context: Context, bitmap: Bitmap, onFacesDetected: (List<RectF>) -> Unit) {
        // Configure the Face Detector options

        val baseOptionsBuilder = BaseOptions.builder()
        val modelName = "face_detection_full_range.tflite"

        baseOptionsBuilder.setModelAssetPath(modelName)

        val optionsBuilder =
            FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinDetectionConfidence(0.5f)
                .setRunningMode(RunningMode.IMAGE)
                .build()

        // Create Face Detector
        val faceDetector = FaceDetector.createFromOptions(context, optionsBuilder)

        // Convert Bitmap to MediaPipe Image
        val image = BitmapImageBuilder(bitmap).build()

        // Detect Faces
        val result = faceDetector.detect(image)

        // Get bounding boxes
        val faceBoxes = result.detections().map { detection ->
            RectF(
                detection.boundingBox().left,
                detection.boundingBox().top,
                detection.boundingBox().right,
                detection.boundingBox().bottom,
            )
        }

        // Callback with the detected faces
        onFacesDetected(faceBoxes)
    }

    fun drawBoundingBoxesAndTags(
        bitmap: Bitmap,
        faceBoxes: List<RectF>,
        tags: List<String> = emptyList()
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        val textPaint = Paint().apply {
            color = Color.BLUE
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }

        faceBoxes.forEachIndexed { index, rect ->
            // Draw bounding box
            canvas.drawRect(rect, boxPaint)

            // Draw tag
            val tag = tags.getOrNull(index) ?: "Face ${index + 1}"
            canvas.drawText(tag, rect.left, rect.top - 10, textPaint)
        }

        return mutableBitmap
    }

}