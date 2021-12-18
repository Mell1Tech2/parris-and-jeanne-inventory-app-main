package com.example.inventory.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScannedBarcodes: ViewModel(){

    private val barcode: MutableLiveData<String> by lazy {
        MutableLiveData<String>("")
    }

    fun setBarcode(item: String) {
        barcode.value = item
        Log.d("Log", "Set barcode: " + barcode.value as String)
    }

    fun getBarcode(): LiveData<String> {
        Log.d("Log", "Get barcode: " + barcode.value as String)
        return barcode
    }
}