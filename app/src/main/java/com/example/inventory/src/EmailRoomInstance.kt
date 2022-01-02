package com.example.inventory.src

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.example.inventory.InventoryViewModel
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.io.IOException

class EmailRoomInstance {

    fun backupDatabase(activity: FragmentActivity, viewModel: InventoryViewModel) {

        Log.d("Log", "backup started")

        val csvFile = File(activity.filesDir, "item_database.csv")
        csvFile.createNewFile()
        Log.d("Log", "dbFileExists: " + csvFile.exists())

        exportDatabaseToCSVFile(csvFile, viewModel)

        try {
            // Get Uri of the copied database file from filesDir to be used in email intent
            val uri = getUri(activity.applicationContext, csvFile)
            Log.d("Log", "Uri complete")

            sendEmail(activity, uri)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun exportDatabaseToCSVFile(csvFile: File, viewModel: InventoryViewModel) {

        val itemList = viewModel.allItems.value
        //csvWriter().writeAll(rows, "test.csv", append = true)

        csvWriter().open(csvFile, append = false) {

            writeRow("Item Barcode", "Item Name", "Item Price", "Item Quantity")
            Log.d("Log", "Item List  $itemList")
            itemList?.forEach {
                writeRow(it.itemBarcode, it.itemName, it.itemPrice, it.quantityInStock)
            }
        }
        Log.d("Log", "Item List $csvFile")
    }

    private fun getUri(context: Context, file: File): Uri {

        var uri = Uri.fromFile(file)
        Log.d("Log", "uri: $uri")

        // Override for newer android SDK's
        if (Build.VERSION.SDK_INT >= 24) {
            Log.d("Log", "SDK greater than 24")
            uri = FileProvider.getUriForFile(
                context,
                "com.inventory.example.provider", file
            )
        }
        return uri
    }

    private fun sendEmail(activity: FragmentActivity, attachment: Uri) {

        val emailIntent = Intent(Intent.ACTION_SEND)
        Log.d("Log", "intent action complete")

        emailIntent.type = "vnd.android.cursor.dir/email"
        Log.d("Log", "email type complete")

        val toEmail = "KieranAbelen@gmail.com"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, toEmail)
        Log.d("Log", "email EXTRA_EMAIL complete")

        emailIntent.putExtra(Intent.EXTRA_STREAM, attachment)
        Log.d("Log", "email EXTRA_STREAM complete")

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Stocktaking data")
        Log.d("Log", "email: $emailIntent")

        activity.startActivity(Intent.createChooser(emailIntent, "Send Email"))
        Log.d("Log", "email Sent")
    }
}