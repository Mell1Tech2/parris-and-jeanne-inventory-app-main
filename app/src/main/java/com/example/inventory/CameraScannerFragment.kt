package com.example.inventory

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface.ROTATION_0
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.inventory.barcode.ImageAnalyzer
import com.example.inventory.data.SessionAddItem
import com.example.inventory.databinding.FragmentCameraScannerBinding
import com.example.inventory.shapedrawable.DrawBoxView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScannerFragment : Fragment() {

    private val sessionAddItem: SessionAddItem by activityViewModels()
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var drawBoxView: DrawBoxView
    private var dummyButton: Button? = null
    private var _binding: FragmentCameraScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraScannerBinding.inflate(inflater, container, false)
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
    @SuppressLint("UnsafeOptInUsageError")
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
                .setTargetResolution(Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setImageQueueDepth(1)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        ImageAnalyzer(
                            this.requireActivity(),
                            this,
                            drawBoxView,
                            binding.previewView.width.toFloat(),
                            binding.previewView.height.toFloat(),
                            sessionAddItem,
                        )

                    )
                }

            //val imageCapture = ImageCapture.Builder().build()

            val viewPort =  ViewPort.Builder(Rational(560, 480), ROTATION_0).build()
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalyzer)
                //.addUseCase(imageCapture)
                .setViewPort(viewPort)
                .build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA



            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, useCaseGroup
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
        dummyButton = binding.dummyButton
        binding.dummyButton.setOnClickListener {
            returntoAddItem()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        drawBoxView = binding.DrawBoxViewBind
        checkCameraPermission()
    }
}