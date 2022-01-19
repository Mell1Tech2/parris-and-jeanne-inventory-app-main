package com.example.inventory.barcode

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.inventory.CameraScannerFragment
import com.example.inventory.data.SessionAddItem
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.example.inventory.shapedrawable.DrawBoxView
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ImageAnalyzer(
    private val activity: Activity,
    private val context: CameraScannerFragment,
    private val drawBoxView: DrawBoxView,
    private val previewViewWidth: Float,
    private val previewViewHeight: Float,
    private val sessionAddItem: SessionAddItem,

    ) : ImageAnalysis.Analyzer {

    // This parameters will handle preview box scaling
    private var scaleX = 1f
    private var scaleY = 1f

    private fun translateX(x: Float) = x * scaleX
    private fun translateY(y: Float) = y * scaleY

    private fun adjustBoundingRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val img = image.image

        if (img != null) {
            // Update scale factors
            scaleX = previewViewWidth / img.height.toFloat()
            scaleY = previewViewHeight / img.width.toFloat()

            val inputImage = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)

            // Process image searching for barcodes
            val optionsBarcode = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_CODABAR,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_ITF,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E
                )
                .build()


            val scanner = BarcodeScanning.getClient(optionsBarcode)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            if ("readText" == sessionAddItem.getState().value) {
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        Log.d("Log", "barcode listener")
                        if (visionText.textBlocks.size != 0) {
                            for (block in visionText.textBlocks) {
                                val blockText = block.text
                                block.boundingBox?.let { rect ->
                                    drawBoxView.setRect(
                                        adjustBoundingRect(
                                            rect
                                        )
                                    )
                                }
                                sessionAddItem.setName(blockText + "\n")
                            }
                        }
                    }
            }
            else if("barcode" == sessionAddItem.getState().value) {
                scanner.detectorType.times(1)

                scanner.process(inputImage)

                    .addOnSuccessListener { barcodes ->

                        Log.d("Log", "barcode listener")
                        if (barcodes.isNotEmpty()) {
                            Log.d("Log", "barcode not empty")
                            for (barcode in barcodes) {
                                sessionAddItem.setBarcode(barcode?.rawValue.toString())
                                Log.d("Log", "barcode success")
                                // Update bounding rect
                                barcode.boundingBox?.let { rect ->
                                    drawBoxView.setRect(
                                        adjustBoundingRect(
                                            rect
                                        )
                                    )
                                }
                                activity.onBackPressed()
                            }
                        }
                    }
            }
        }
        image.close()
        }
    }

