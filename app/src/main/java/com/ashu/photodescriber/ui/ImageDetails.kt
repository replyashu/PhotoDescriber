package com.ashu.photodescriber.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ashu.photodescriber.databinding.ActivityDetailsBinding
import com.ashu.photodescriber.helper.ImageHelperUtil
import com.ashu.photodescriber.helper.ImageHelperUtil.drawBoundingBoxesAndTags
import com.ashu.photodescriber.helper.toBitmap
import com.bumptech.glide.Glide
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageDetails: AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val img = intent.extras?.get("image_url")
        val original = intent.extras?.getString("original_image")

        val bitmap = Uri.parse(img.toString()).toBitmap(this)
        var updatedBitmap: Bitmap? = null

        Glide.with(this)
            .load(bitmap)
            .into(binding.imgUser)

        val tags = viewModel.getTags(img.toString())
        val baseOptionsBuilder = BaseOptions.builder()
        val modelName = "face_detection_full_range.tflite"

        baseOptionsBuilder.setModelAssetPath(modelName)
        val optionsBuilder =
            FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinDetectionConfidence(0.5f)
                .setRunningMode(RunningMode.IMAGE)
                .build()
        val faceDetector = FaceDetector.createFromOptions(this, optionsBuilder)
        if (bitmap != null) {
            ImageHelperUtil.detectFaces(this, bitmap) { faceBoxes ->
                enableTagEditing(this, binding.imgUser, faceBoxes, tags) { newTags ->
                    updatedBitmap = drawBoundingBoxesAndTags(bitmap, faceBoxes, newTags)

                    viewModel.saveBitmap(updatedBitmap!!, original!!)
                }
            }

            if (updatedBitmap != null) {
                Glide.with(this)
                    .load(updatedBitmap)
                    .into(binding.imgUser)
            }
        }

    }

    private fun enableTagEditing(
        context: Context,
        imageView: ImageView,
        faceBoxes: List<RectF>,
        tags: MutableList<String>,
        onTagsUpdated: (List<String>) -> Unit
    ) = imageView.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x
            val y = event.y

            // Check if touch is within any face bounding box
            val touchedIndex = faceBoxes.indexOfFirst { rect ->
                rect.contains(x, y)
            }

            if (touchedIndex >= -1) {
                // Show dialog to edit tag
                val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                dialog.setTitle("Edit Tag for Face ${touchedIndex + 1}")

                val input = android.widget.EditText(context)
                input.setText(tags[touchedIndex + 1])
                dialog.setView(input)

                dialog.setPositiveButton("OK") { _, _ ->
                    // Update tag
                    tags[touchedIndex] = input.text.toString()
                    onTagsUpdated(tags)
                }

                dialog.setNegativeButton("Cancel", null)
                dialog.show()
            }
        }
        true
    }
}