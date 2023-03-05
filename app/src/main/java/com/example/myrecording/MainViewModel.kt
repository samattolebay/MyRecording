package com.example.myrecording

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _records = MutableLiveData<List<Record>>()
    val records: LiveData<List<Record>> = _records

    fun onRecordClick() {
        _isRecording.value = isRecording.value == false
    }

    fun onStopRecording(
        uptimeMillis: Long,
        fileName: String,
        updatedTime: Long,
        createdDate: String
    ) {
        val curRecords = records.value?.toMutableList() ?: mutableListOf()
        curRecords.add(Record(uptimeMillis, fileName, fileName, createdDate, updatedTime))
        _records.value = curRecords
    }

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying
}