package com.example.expressionphotomanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle

import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley


import org.json.JSONObject
import java.io.ByteArrayOutputStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException


class CameraActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var button: TextView
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private var cameraPreviewSize: Size? = null
    private lateinit var requestQueue: com.android.volley.RequestQueue

    private var byteArray: ByteArray? = null

    val emotions = arrayOf("Smile", "Sad", "Angry", "Surprise")
    val randomEmotion = emotions.random()

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val API_KEY = "AIzaSyB-HR7-Wnry3uAPLXk55nKqLXILIqXN1yM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        requestQueue = Volley.newRequestQueue(this)
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = getTextureListener()

        button = findViewById(R.id.button_capture)


        button.text = randomEmotion
        button.setOnClickListener {
            takePicture()
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun getTextureListener() = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            adjustAspectRatio(
                textureView.width,
                textureView.height,
                cameraPreviewSize?.width ?: width,
                cameraPreviewSize?.height ?: height
            )
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun openCamera() {
        val cameraManager: CameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            } ?: return

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            cameraPreviewSize = streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java)?.maxByOrNull { it.height * it.width }

            cameraManager.openCamera(cameraId, stateCallback, null)

            // Adjust aspect ratio here after setting the preview size
            cameraPreviewSize?.let {
                adjustAspectRatio(textureView.width, textureView.height, it.width, it.height)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            adjustAspectRatio(textureView.width, textureView.height, cameraPreviewSize?.width ?: width, cameraPreviewSize?.height ?: height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }


    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreviewSession() {
        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(cameraPreviewSize?.width ?: textureView.width, cameraPreviewSize?.height ?: textureView.height)
        val surface = Surface(texture)
        captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) ?: return
        captureRequestBuilder.addTarget(surface)

        cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                if (cameraDevice == null) return
                cameraCaptureSessions = session
                updatePreview()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(this@CameraActivity, "Configuration failed.", Toast.LENGTH_SHORT).show()
            }
        }, null)
    }

    private fun updatePreview() {
        if (cameraDevice == null) return
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        val request = captureRequestBuilder.build()
        cameraCaptureSessions?.setRepeatingRequest(request, null, null)
    }

    private fun takePicture() {
        Toast.makeText(this@CameraActivity, "Image taken.", Toast.LENGTH_SHORT).show()
        cameraDevice?.let { device ->
            val surfaceTexture = textureView.surfaceTexture
            surfaceTexture?.setDefaultBufferSize(cameraPreviewSize!!.width, cameraPreviewSize!!.height)
            val captureSurface = Surface(surfaceTexture)

            val captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(captureSurface)
                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(this@CameraActivity, "ProcessImage called.", Toast.LENGTH_SHORT).show()
                    processImage()
                }
            }

            try {
                cameraCaptureSessions?.stopRepeating()
                cameraCaptureSessions?.capture(captureBuilder.build(), captureCallback, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }


    private fun processImage() {
        textureView.bitmap?.let { bitmap ->
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                byteArray = stream.toByteArray()
                sendImageToServer(byteArray!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } ?: Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
    }


    private fun sendImageToServer(imageBytes: ByteArray) {
        // Convert the byte array to a Base64-encoded string
        val base64EncodedString = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        // Create the JSON body for the request
        val jsonRequestBody = """
        {
            "requests": [
                {
                    "image": {
                        "content": "$base64EncodedString"
                    },
                    "features": [
                        {
                            "type": "FACE_DETECTION"
                        }
                    ]
                }
            ]
        }
    """.trimIndent()

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonRequestBody)


        val request = Request.Builder()
            .url("https://vision.googleapis.com/v1/images:annotate?key=$API_KEY") // Replace YOUR_API_KEY with your actual API key
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CameraActivity, "Failed to send image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject(responseBody)
                        val faceAnnotations = jsonObject.getJSONArray("responses").getJSONObject(0).getJSONArray("faceAnnotations").getJSONObject(0)

                        val apiEmotions = mapOf(
                            "joyLikelihood" to "Smile",
                            "sorrowLikelihood" to "Sad",
                            "angerLikelihood" to "Angry",
                            "surpriseLikelihood" to "Surprise"
                        )

                        // Check if the detected emotion is 'VERY_LIKELY' and matches the randomEmotion
                        val matchingEmotion = apiEmotions.entries.firstOrNull {
                            faceAnnotations.getString(it.key) == "VERY_LIKELY" && it.value == randomEmotion
                        }?.value

                        if (matchingEmotion != null) {
                            // Convert image to byte array
                            val imageData: ByteArray = byteArray!!

                            // Save to SQLite database
                            val dbHelper = DatabaseHelper(this@CameraActivity)
                            dbHelper.addImage(imageData, matchingEmotion)

                            // Intent to ResultActivity with "Good" result
                            val intent = Intent(this@CameraActivity, DisplayImagesActivity::class.java).apply {
                            }
                            startActivity(intent)
                        } else {
                            // Intent to ResultActivity with "Bad" result
                            val intent = Intent(this@CameraActivity, CameraActivity::class.java).apply {

                            }
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@CameraActivity, "Failed to send image. Response Code: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }

    private fun adjustAspectRatio(textureWidth: Int, textureHeight: Int, previewWidth: Int?, previewHeight: Int?) {
        if (previewWidth == null || previewHeight == null) return

        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, textureWidth.toFloat(), textureHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewHeight.toFloat(), previewWidth.toFloat()) // Swap to match orientation
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (textureWidth > textureHeight) {
            // Landscape
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(textureHeight.toFloat() / previewHeight, textureWidth.toFloat() / previewWidth)
            matrix.postScale(scale, scale, centerX, centerY)
        } else {
            // Portrait or square
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(textureWidth.toFloat() / previewHeight, textureHeight.toFloat() / previewWidth)
            matrix.postScale(scale, scale, centerX, centerY)
        }

        textureView.setTransform(matrix)
    }

}
