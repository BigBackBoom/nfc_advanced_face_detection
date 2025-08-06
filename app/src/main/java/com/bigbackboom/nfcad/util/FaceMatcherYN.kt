package com.bigbackboom.nfcad.util

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RawRes
import androidx.core.graphics.createBitmap
import org.opencv.R
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.FaceDetectorYN
import org.opencv.objdetect.FaceRecognizerSF
import java.io.File
import java.io.FileOutputStream


class FaceMatcherYN(context: Context) {

    private val faceRecognizer: FaceRecognizerSF

    data class FaceDetectResult(
        val faces: Mat,
        val faceFeature: Mat,
        val targetMat: Mat
    )

    data class RaceMatchResult(
        val score: Double,
        val targetMat: Bitmap
    )

    init {
        OpenCVLoader.initLocal()

        // Face recognizer initialization
        val modelFaceSf = readModel(context, R.raw.face_recognition_sface_2021dec, "face_recognition_sface_2021dec.onnx")
        faceRecognizer = FaceRecognizerSF.create(modelFaceSf, "")
    }

    fun detectFace(context: Context, image: Bitmap): FaceDetectResult? {

        // Face detector initialization
        val modelFaceDetect = readModel(context, R.raw.face_detection_yunet_2023mar, "face_detection_yunet_2023mar.onnx")
        val detector = FaceDetectorYN.create(modelFaceDetect, "", Size(image.width.toDouble(), image.height.toDouble()), 0.3f, 0.6f, 1, Dnn.DNN_BACKEND_DEFAULT, Dnn.DNN_TARGET_CPU)
        val mat = Mat().apply {
            Utils.bitmapToMat(image, this, false)
        }

        // color conversion only rgb is accepted
        val targetMat = Mat()
        Imgproc.cvtColor(mat, targetMat, Imgproc.COLOR_RGBA2RGB)

        // detect faces
        val faces = Mat()
        detector.inputSize = targetMat.size()
        detector.detect(targetMat, faces)

        if (faces.empty()) return null

        val alignedImage = Mat()
        faceRecognizer.alignCrop(targetMat, faces.row(0), alignedImage)

        val faceFeature = Mat()
        faceRecognizer.feature(alignedImage, faceFeature)

        return FaceDetectResult(
            faces = faces,
            faceFeature = faceFeature,
            targetMat = targetMat
        )
    }

    fun match(original: FaceDetectResult, target: FaceDetectResult): RaceMatchResult {
        val score = faceRecognizer.match(original.faceFeature, target.faceFeature, FaceRecognizerSF.FR_COSINE)
        val faces = target.faces
        val targetMat = target.targetMat

        Imgproc.rectangle(
            targetMat,
            Rect((faces.get(0, 0)[0]).toInt(), (faces.get(0, 1)[0]).toInt(), (faces.get(0, 2)[0]).toInt(), (faces.get(0, 3)[0]).toInt()),
            Scalar(255.0, 0.0, 0.0), 3
        )

        Imgproc.circle(
            targetMat,
            Point(faces.get(0, 4)[0], faces.get(0, 5)[0]),
            2,
            Scalar(255.0, 0.0, 0.0),
            2
        )
        Imgproc.circle(
            targetMat,
            Point(faces.get(0, 6)[0], faces.get(0, 7)[0]),
            2,
            Scalar(0.0, 0.0, 255.0),
            2
        )
        Imgproc.circle(
            targetMat,
            Point(faces.get(0, 8)[0], faces.get(0, 9)[0]),
            2,
            Scalar(0.0, 255.0, 0.0),
            2
        )
        Imgproc.circle(
            targetMat,
            Point(faces.get(0, 10)[0], faces.get(0, 11)[0]),
            2,
            Scalar(255.0, 0.0, 255.0),
            2
        )
        Imgproc.circle(
            targetMat,
            Point(faces.get(0, 12)[0], faces.get(0, 13)[0]),
            2,
            Scalar(0.0, 255.0, 255.0),
            2
        )

        val percent = ((score / COSINE_THRESHOLD_MAX) * 100.0).coerceIn(0.0, 100.0)
        Imgproc.putText(targetMat, "Score: %.2f".format(percent), Point(faces.get(0, 0)[0], faces.get(0, 1)[0] - 50), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 3)

        return RaceMatchResult(
            score = (score / COSINE_THRESHOLD_MAX) * 100.0,
            targetMat = createBitmap(targetMat.cols(), targetMat.rows()).apply {
                Utils.matToBitmap(targetMat, this)
            }
        )
    }

    private fun readModel(context: Context, @RawRes raw: Int, name: String): String {
        val content = context.resources.openRawResource(raw)
        val modelDir = context.getDir("model", Context.MODE_PRIVATE)
        val modelFile = File(modelDir, name)

        val os = FileOutputStream(modelFile)

        val buffer = ByteArray(4096)
        var bytesRead: Int
        while ((content.read(buffer).also { bytesRead = it }) != -1) {
            os.write(buffer, 0, bytesRead)
        }

        content.close()
        os.close()

        return modelFile.absolutePath
    }

    companion object {
        const val COSINE_THRESHOLD = 0.363 // 99.6% threshold
        const val COSINE_THRESHOLD_MAX = 0.365 // normalized 100%
        const val NORML2_THRESHOLD = 1.128
    }
}
