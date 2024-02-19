package com.example.mlkitpractice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity(), AdapterView.OnItemSelectedListener {
    private var mSelectedImage: Bitmap? = null
    lateinit var imageView:ImageView
    private var mImageMaxWidth: Int? = null
    private var mImageMaxHeight: Int? = null
    private var mTextButton: Button? = null
    private var mFaceButton: Button? = null
    private var textView:TextView? = null
    private var mGraphicOverlay: GraphicOverlay? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dropdown: Spinner = findViewById(R.id.spinner)
        imageView = findViewById(R.id.image_view)
        textView = findViewById(R.id.tv_text)
        mTextButton = findViewById(R.id.button_text)
        mFaceButton = findViewById(R.id.button_face)
        mGraphicOverlay = findViewById(R.id.graphic_overlay)

        mTextButton?.setOnClickListener {
            //runTextRecognition()
            runLabelRecognition()
        }
        mFaceButton?.setOnClickListener {
            runFaceContourDetection()
        }
        val items = arrayOf<String?>("Test Image 1 (Text)", "Test Image 2 (Face)")
        val adapter: ArrayAdapter<Any?> =
            ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
        dropdown.setOnItemSelectedListener(this)


    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        mGraphicOverlay!!.clear()

        when (p2) {
            0 -> mSelectedImage =
                //getBitmapFromAsset(this,"handwritten_text.png")
                //getBitmapFromAsset(this, "willienelson1.jpg")
            getBitmapFromAsset(this, "please_walk_on_the_grass.jpg")

            1 ->                 // Whatever you want to happen when the thrid item gets selected
                mSelectedImage =
                    //getBitmapFromAsset(this, "human_faces.jpg")
            getBitmapFromAsset(this,"man_multitasking.jpg")

                    //getBitmapFromAsset(this, "child.jpg")
                    //getBitmapFromAsset(this, "cartoon.png")
            //getBitmapFromAsset(this, "images.jpg")
            //getBitmapFromAsset(this, "old_person.jpg")
        }
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            val targetedSize: Pair<Int, Int> = getTargetedWidthHeight()!!
            val targetWidth = targetedSize.first
            val maxHeight = targetedSize.second

            // Determine how much to scale down the image
            val scaleFactor = Math.max(
                mSelectedImage!!.getWidth().toFloat() / targetWidth.toFloat(),
                mSelectedImage!!.getHeight().toFloat() / maxHeight.toFloat()
            )
            val resizedBitmap = Bitmap.createScaledBitmap(
                mSelectedImage!!,
                (mSelectedImage!!.getWidth() / scaleFactor).toInt(),
                (mSelectedImage!!.getHeight() / scaleFactor).toInt(),
                true
            )
            imageView.setImageBitmap(resizedBitmap)
            mSelectedImage = resizedBitmap
        }    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
        val assetManager = context.assets
        val `is`: InputStream
        var bitmap: Bitmap? = null
        try {
            `is` = assetManager.open(filePath!!)
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun getTargetedWidthHeight(): Pair<Int, Int>? {
        val targetWidth: Int
        val targetHeight: Int
        val maxWidthForPortraitMode: Int = getImageMaxWidth()!!
        val maxHeightForPortraitMode: Int = getImageMaxHeight()!!
        targetWidth = maxWidthForPortraitMode
        targetHeight = maxHeightForPortraitMode
        return Pair(targetWidth, targetHeight)
    }

    // Functions for loading images from app assets.
    private fun getImageMaxWidth(): Int? {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = imageView.getWidth()
        }
        return mImageMaxWidth
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private fun getImageMaxHeight(): Int? {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight = imageView.getHeight()
        }
        return mImageMaxHeight
    }

    private fun runTextRecognition() {
        val image = InputImage.fromBitmap(mSelectedImage!!, 0)
        val recognizer = TextRecognition.getClient()
        mTextButton?.setEnabled(false)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                mTextButton?.setEnabled(true)
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                mTextButton?.setEnabled(true)
                e.printStackTrace()
            }
    }

    private fun processTextRecognitionResult(texts: Text) {
        textView?.text = ""
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(this,"No text found",Toast.LENGTH_LONG).show()
            return
        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val elements = lines[j].elements
                for (k in elements.indices) {
                    textView?.text = textView?.text.toString() +" "+ elements[k].text
                }
            }
        }
    }

    private fun processLabelRecognitionResult(labels: List<ImageLabel>){
        textView?.text = ""
        for (label in labels) {
            val text = label.text
            val confidence = label.confidence
            val index = label.index
            textView?.text = textView?.text.toString() +" "+text
        }
    }

    private fun runLabelRecognition() {
        val image = InputImage.fromBitmap(mSelectedImage!!, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        mTextButton?.setEnabled(false)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                mTextButton?.setEnabled(true)
                processLabelRecognitionResult(labels)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                mTextButton?.setEnabled(true)
                e.printStackTrace()
            }
    }

    private fun runFaceContourDetection() {
        val image = InputImage.fromBitmap(mSelectedImage!!, 0)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
        mFaceButton!!.isEnabled = false
        val detector = FaceDetection.getClient(options)
        detector.process(image)
            .addOnSuccessListener { faces ->
                mFaceButton!!.isEnabled = true
                processFaceContourDetectionResult(faces)
            }
            .addOnFailureListener { e -> // Task failed with an exception
                mFaceButton!!.isEnabled = true
                e.printStackTrace()
            }
    }

    private fun processFaceContourDetectionResult(faces: List<Face>) {
        // Task completed successfully
        if (faces.size == 0) {
            Toast.makeText(this,"No face found",Toast.LENGTH_LONG).show()
            return
        }
        mGraphicOverlay?.clear()
        for (i in faces.indices) {
            val face = faces[i]
            val faceGraphic = FaceContourGrapic(mGraphicOverlay)
            mGraphicOverlay?.add(faceGraphic)
            faceGraphic.updateFace(face)
        }
    }


}