package com.ashu.photodescriber.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ashu.photodescriber.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var photoAdapter: MainAdapter

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_IMAGE_PERMISSION = 100
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        System.loadLibrary("mediapipe_jni")
//        System.loadLibrary("opencv_java3")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setUpView()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            viewModel.readGalleryImages()
        } else {
            startLocationPermissionRequest()
        }

        lifecycleScope.launch {
            viewModel.readImage.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {
                updateState(it)
            }
        }
    }

    private fun updateState(state: MainViewModel.ImagesState) {
        when(state) {
            is MainViewModel.ImagesState.Loading -> {
                binding.pbImages.visibility = View.VISIBLE
            }
            is MainViewModel.ImagesState.Success -> {
                binding.pbImages.visibility = View.GONE
                binding.rvImages.visibility = View.VISIBLE
                photoAdapter.submitList(state.images)
            }
            is MainViewModel.ImagesState.Error -> {}
        }
    }

    private fun startLocationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.readGalleryImages()
        } else {
            requestForPermission()
        }
    }

    private fun requestForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES), REQUEST_IMAGE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_IMAGE_PERMISSION
            )
        }
    }

    private fun setUpView() {
        setContentView(binding.root)
        binding.rvImages.layoutManager = GridLayoutManager(this, 3)
        binding.rvImages.adapter = photoAdapter
    }

}