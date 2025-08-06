package com.bigbackboom.nfcad.util

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.AKAZE
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.Features2d


class FaceMatcher {

    fun init(): Boolean {
        return OpenCVLoader.initLocal();
    }

    fun match(original: Bitmap, target: Bitmap): MatchResult {
        val akaze = AKAZE.create()
        val matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE)

        // Create Matrix
        val targetMat = Mat().apply {
            Utils.bitmapToMat(target, this)
        }
        val originalMat = Mat().apply {
            Utils.bitmapToMat(original, this)
        }

        // init feature descriptor
        val targetKeypoints = MatOfKeyPoint()
        val originalKeypoints = MatOfKeyPoint()
        val targetDescriptors = Mat()
        val originalDescriptors = Mat()

        with(targetMat) {
            akaze.detect(this, targetKeypoints)
            akaze.compute(this, targetKeypoints, targetDescriptors)
        }

        with(originalMat) {
            akaze.detect(this, originalKeypoints)
            akaze.compute(this, originalKeypoints, originalDescriptors)
        }

        // Match descriptors
        val matchVector: MutableList<MatOfDMatch?> = ArrayList()
        matcher.knnMatch(originalDescriptors, targetDescriptors, matchVector, 2)

        //
        val threshold = 1.0f
        val goodMatches: MutableList<MatOfDMatch?> = ArrayList()
        for (i in matchVector.indices) {
            val vector = matchVector[i]?.toArray() ?: continue

            Log.d(
                "FaceMatcher",
                "Match: ${vector[0].imgIdx} -> ${vector[0].distance}, ${vector[1].imgIdx} -> ${vector[1].distance}"
            )
            if (vector[0].distance < threshold * vector[1].distance) {
                Log.d(
                    "FaceMatcher",
                    "Good Match: ${vector[0].distance}, ${vector[1].distance * threshold}"
                )
                goodMatches.add(matchVector[i])
            }
        }
        return MatchResult(
            MatrixEvaluation(
                originalMat,
                originalKeypoints
            ),
            MatrixEvaluation(
                targetMat,
                targetKeypoints
            ),
            goodMatches.slice(0..10),
            matchVector
        )
    }

    fun createComparisonImage(result: MatchResult): Bitmap {

        val matchedImage = Mat()
        Features2d.drawMatchesKnn(
            result.original.mat,
            result.original.keypoints,
            result.target.mat,
            result.target.keypoints,
            result.goodMatchVector,
            matchedImage
        )
        val outputBmp = createBitmap(matchedImage.cols(), matchedImage.rows())
        Utils.matToBitmap(matchedImage, outputBmp)
        return outputBmp
    }

    data class MatrixEvaluation(
        val mat: Mat,
        val keypoints: MatOfKeyPoint
    )

    data class MatchResult(
        val original: MatrixEvaluation,
        val target: MatrixEvaluation,
        val goodMatchVector: List<MatOfDMatch?>,
        val allMatchesVector: List<MatOfDMatch?>
    )

}