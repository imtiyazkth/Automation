package com.personalai.os.core.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LiveAudioCapture(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun start(scope: CoroutineScope, onChunk: (ByteArray) -> Unit) {
        if (!hasPermission()) return
        val sampleRate = 16000
        val minBufSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = if (minBufSize > 0) minBufSize * 2 else sampleRate

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return
        }
        audioRecord = record
        record.startRecording()

        captureJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(4096)
            while (audioRecord != null) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) onChunk(buffer.copyOf(read))
            }
        }
    }

    fun stop() {
        captureJob?.cancel()
        captureJob = null
        audioRecord?.let {
            runCatching { it.stop() }
            it.release()
        }
        audioRecord = null
    }
}
