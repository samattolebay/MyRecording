package com.example.myrecording

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import timber.log.Timber
import java.io.IOException
import java.util.*


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity(), OnClickListener {

    private var fileName: String = ""
    private var createdDate: String = ""

    //    private var recordButton: RecordButton? = null
    private var recorder: MediaRecorder? = null
    private var recordButton: FloatingActionButton? = null

    private var recordsList: RecyclerView? = null

    //    private var playButton: PlayButton? = null
    private var player: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private val viewModel: MainViewModel by viewModels()

    private var timeInMilliSeconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L
    private var startTime = 0L

    private val handler = Handler(Looper.getMainLooper())

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliSeconds = SystemClock.uptimeMillis() - startTime
            updatedTime = timeSwapBuff + timeInMilliSeconds
            handler.postDelayed(this, 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setOnPreparedListener {
                    it.start()
                }
                setDataSource(fileName)
                prepareAsync()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        createdDate = Calendar.getInstance().time.toString()
        fileName = "${externalCacheDir?.absolutePath}/$createdDate.3gp"
        println(fileName)
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }

            start()
            startTime = SystemClock.uptimeMillis()
            handler.postDelayed(updateTimerThread, 0)
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            timeSwapBuff += timeInMilliSeconds
            handler.removeCallbacks(updateTimerThread)
            release()
            viewModel.onStopRecording(
                SystemClock.uptimeMillis(),
                fileName,
                updatedTime,
                createdDate
            )
        }
        recorder = null
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Record to the external cache directory for visibility

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        setContentView(R.layout.activity_main)

        supportActionBar?.title = getString(R.string.app_bar_title)

        val recordsAdapter = RecordsAdapter {
            // todo on play click
        }
        recordsList = findViewById<RecyclerView?>(R.id.recordsList).apply {
            adapter = recordsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        recordButton = findViewById<FloatingActionButton>(R.id.recordButton).apply {
            setOnClickListener(this@MainActivity)
        }

        viewModel.isRecording.observe(this) {
            onRecord(it)
        }

        viewModel.isPlaying.observe(this) {
            onPlay(it)
        }

        viewModel.records.observe(this) {
            recordsAdapter.submitList(it)
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.recordButton -> {
                viewModel.onRecordClick()
            }

        }
    }
}