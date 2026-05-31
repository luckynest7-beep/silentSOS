package com.example.services

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): String? {
        try {
            val outputDir = context.filesDir
            currentFile = File.createTempFile("sos_audio_", ".mp4", outputDir)
            
            @Suppress("DEPRECATION")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentFile!!.absolutePath)
                prepare()
                start()
            }
            Log.d("AudioService", "Audio recording started at: ${currentFile!!.absolutePath}")
            return currentFile!!.absolutePath
        } catch (e: Exception) {
            Log.e("AudioService", "Failed to start audio recording", e)
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            return null
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            Log.d("AudioService", "Audio recording stopped")
        } catch (e: Exception) {
            Log.e("AudioService", "Failed to stop audio recording or was already stopped", e)
        } finally {
            mediaRecorder = null
        }
    }
}
