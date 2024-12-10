package com.ashu.photodescriber.ui

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashu.photodescriber.helper.ImageHelperUtil
import com.ashu.photodescriber.helper.toBitmap
import com.ashu.photodescriber.repository.db.ImageRepository
import com.ashu.photodescriber.repository.db.entity.UserImages
import com.google.mediapipe.framework.AndroidPacketCreator
import com.google.mediapipe.framework.Graph
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val context: Context,
                                        private val imageRepository: ImageRepository): ViewModel() {
    private var _readImages = MutableStateFlow<ImagesState>(ImagesState.Loading)
    val readImage: Flow<ImagesState> = _readImages
    private lateinit var faceDetectionGraph: Graph
    private lateinit var packetCreator: AndroidPacketCreator

    init {
//        faceDetectionGraph = Graph()
//        packetCreator = AndroidPacketCreator(faceDetectionGraph)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _readImages.emit(ImagesState.Loading)
                try {
                    imageRepository.getImages()
                        .collect { _images ->
                            if (_images.isNotEmpty()) {
                                _readImages.emit(ImagesState.Success(_images))
                            }
                    }
                } catch (e: Exception) {
                    _readImages.update { ImagesState.Error }
                }
            }
        }
    }
    fun readGalleryImages() = viewModelScope.launch {
        withContext(Dispatchers.IO) {

            val baseOptionsBuilder = BaseOptions.builder()
            val modelName = "face_detection_full_range.tflite"

            baseOptionsBuilder.setModelAssetPath(modelName)

            val optionsBuilder =
                FaceDetector.FaceDetectorOptions.builder()
                    .setBaseOptions(baseOptionsBuilder.build())
                    .setMinDetectionConfidence(0.5f)
                    .setRunningMode(RunningMode.IMAGE)
                    .build()

            val faceDetector = FaceDetector.createFromOptions(context, optionsBuilder)
            val imageProjection = arrayOf(
                MediaStore.Images.Media._ID
            )

            val imageSortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                imageSortOrder
            )

            cursor.use {

                if (cursor != null) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    while (cursor.moveToNext()) {
                        var uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(idColumn)
                        )
                        uri.let {
                            val bitmap = uri.toBitmap(context)
                            if (bitmap != null && ImageHelperUtil.containsFace(bitmap, faceDetector)) {
                                ImageHelperUtil.detectAndSaveImage(uri.toString(), bitmap, faceDetector, context, imageRepository)
                            }
                        }
                    }
                } else {
                    Log.d("AddViewModel", "Cursor is null!")
                }
            }
        }
    }

    fun getTags(imgPath: String): MutableList<String> {
        var tag = mutableListOf("")
        runBlocking {
            tag =  imageRepository.getTag(imgPath)
        }
        return tag
    }

    fun saveBitmap(bitmap: Bitmap, original: String) = viewModelScope.launch {
        val baseOptionsBuilder = BaseOptions.builder()
        val modelName = "face_detection_full_range.tflite"

        baseOptionsBuilder.setModelAssetPath(modelName)

        val optionsBuilder =
            FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinDetectionConfidence(0.5f)
                .setRunningMode(RunningMode.IMAGE)
                .build()

        val faceDetector = FaceDetector.createFromOptions(context, optionsBuilder)
        ImageHelperUtil.detectAndSaveImage(original, bitmap, faceDetector, context, imageRepository)
    }

    sealed interface ImagesState {
        data object Loading : ImagesState
        data class Success(val images: List<UserImages>) : ImagesState
        data object Error : ImagesState
    }
}