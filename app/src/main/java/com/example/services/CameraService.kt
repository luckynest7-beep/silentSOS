package com.example.services

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraService(private val context: Context) {
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun takeSelfie(lifecycleOwner: LifecycleOwner, onSaved: (String?) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Select front camera
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                // Set up image capture
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // Unbind any previous use cases before binding them
                cameraProvider.unbindAll()
                
                // Bind use cases to lifecycle
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture
                )
                
                // Create storage file
                val outputDir = context.filesDir
                val file = File.createTempFile("sos_selfie_", ".jpg", outputDir)
                
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val absolutePath = file.absolutePath
                            Log.d("CameraService", "Selfie captured successfully: $absolutePath")
                            onSaved(absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraService", "Selfie capture failed: ${exception.message}", exception)
                            onSaved(null)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("CameraService", "Failed to setup camera provider", e)
                onSaved(null)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takeBackPhoto(lifecycleOwner: LifecycleOwner, onSaved: (String?) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Set up image capture
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                // Unbind any previous use cases before binding them
                cameraProvider.unbindAll()
                
                // Bind use cases to lifecycle
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture
                )
                
                // Create storage file
                val outputDir = context.filesDir
                val file = File.createTempFile("sos_back_", ".jpg", outputDir)
                
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                
                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val absolutePath = file.absolutePath
                            Log.d("CameraService", "Back photo captured successfully: $absolutePath")
                            onSaved(absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraService", "Back photo capture failed: ${exception.message}", exception)
                            onSaved(null)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("CameraService", "Failed to setup back camera provider", e)
                onSaved(null)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
