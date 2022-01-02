package com.example.inventory

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.inventory.barcode.QrCodeAnalyzer
import com.example.inventory.data.ScannedBarcodes
import com.example.inventory.databinding.FragmentBarcodeScannerBinding
import com.example.inventory.view.BarcodeBoxView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerFragment : Fragment() {

    private val scannedBarcode: ScannedBarcodes by activityViewModels()

    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        false
    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeBoxView: BarcodeBoxView


    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentBarcodeScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onDestroy() {
        super.onDestroy()
        dummyButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        checkIfCameraPermissionIsGranted()
    }

    /**
     * This function is responsible to request the required CAMERA permission
     */
    private fun checkCameraPermission() = try {
        Log.d("Log", "Checking for camera permission")
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA)
        activity?.let { ActivityCompat.requestPermissions(it, requiredPermissions, 0) }
        Log.d("Log", "Camera permission granted")
        startCamera()
    } catch (e: IllegalArgumentException) {
        checkIfCameraPermissionIsGranted()
    }

    /**
     * This function will check if the CAMERA permission has been granted.
     * If so, it will call the function responsible to initialize the camera preview.
     * Otherwise, it will raise an alert.
     */
    @SuppressLint("WrongConstant")
    private fun checkIfCameraPermissionIsGranted() {
        if (PermissionChecker.checkSelfPermission(this.requireContext(), Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            // Permission granted: start the preview
            Log.d("Log", "Camera permission granted")
            startCamera()
        } else {
            // Permission denied
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle("Permission required")
                .setMessage("This application needs to access the camera to process barcodes")
                .setPositiveButton("Ok") { _, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission()
                }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

    /**
     * This function is responsible for the setup of the camera preview and the image analyzer.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        Log.d("Log", "Camera started")
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            // Image analyzer
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        QrCodeAnalyzer(
                            this.requireActivity(),
                            barcodeBoxView,
                            binding.previewView.width.toFloat(),
                            binding.previewView.height.toFloat(),
                            scannedBarcode,
                        )
                    )
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )


            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    private fun returntoAddItem() {
        // val action = BarcodeScannerFragmentDirections.actionBarcodeScannerFragmentToAddItemFragment(getString(R.string.add_fragment_title))
        // findNavController().navigate(action)
        requireActivity().onBackPressed()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true


        dummyButton = binding.dummyButton
        fullscreenContent = binding.fullscreenContent
        fullscreenContentControls = binding.fullscreenContentControls
        // Set up the user interaction to manually show or hide the system UI.


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.dummyButton.setOnClickListener {
            returntoAddItem()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        barcodeBoxView = BarcodeBoxView(this.requireContext())

        checkCameraPermission()
    }
}