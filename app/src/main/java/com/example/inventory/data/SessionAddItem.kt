package com.example.inventory.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SessionAddItem: ViewModel(){

    private val state: MutableLiveData<String> by lazy {
        MutableLiveData<String>("")
    }
    private val barcodeLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>("")
    }
    private val readTextLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>("")
    }


    fun setState(item: String) {
        state.value = item
        Log.d("Log", "Set barcode: " + state.value as String)
    }

    fun getState(): LiveData<String> {
        Log.d("Log", "Get barcode: " + state.value as String)
        return state
    }

    fun setBarcode(item: String) {
        barcodeLiveData.value = item
        Log.d("Log", "Set barcode: " + barcodeLiveData.value as String)
    }

    fun getBarcode(): LiveData<String> {
        Log.d("Log", "Get barcode: " + barcodeLiveData.value as String)
        return barcodeLiveData
    }

    fun setName(item: String) {
        readTextLiveData.value = item
        Log.d("Log", "Set barcode: " + readTextLiveData.value as String)
    }

    fun getName(): LiveData<String> {
        Log.d("Log", "Get barcode: " + readTextLiveData.value as String)
        return readTextLiveData
    }

}