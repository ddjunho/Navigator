package com.example.festunavigator.data.ml.classification

import android.graphics.Bitmap
import android.graphics.Rect
import android.media.Image
import android.util.Log
import com.example.festunavigator.data.ml.classification.utils.ImageUtils
import com.example.festunavigator.data.model.DetectedObjectResult
import com.example.festunavigator.domain.ml.ObjectDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dev.romainguy.kotlin.math.Float2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Analyzes the frames passed in from the camera and returns any detected text within the requested
 * crop region.
 */
class TextAnalyzer : ObjectDetector {

    private val options = TextRecognizerOptions.Builder().build()
    private val detector = TextRecognition.getClient(options)

    override suspend fun analyze(
        mediaImage: Image,
        rotationDegrees: Int,
        imageCropPercentages: Pair<Int, Int>,
        displaySize: Pair<Int, Int>
    ): Result<DetectedObjectResult> {
        return try {
            val result = withContext(Dispatchers.Default) {
                processImage(mediaImage, rotationDegrees, imageCropPercentages, displaySize)
            }
            result
        } catch (e: Exception) {
            Log.e("TextAnalyzer", "Error during analysis: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun processImage(
        mediaImage: Image,
        rotationDegrees: Int,
        imageCropPercentages: Pair<Int, Int>,
        displaySize: Pair<Int, Int>
    ): Result<DetectedObjectResult> {
        val imageHeight = mediaImage.height
        val imageWidth = mediaImage.width
        val actualAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val bitmap = ImageUtils.convertYuv420888ImageToBitmap(mediaImage)
        var cropRect = Rect(0, 0, imageWidth, imageHeight)

        var currentCropPercentages = imageCropPercentages
        if (actualAspectRatio > 3) {
            val originalHeightCropPercentage = currentCropPercentages.first
            val originalWidthCropPercentage = currentCropPercentages.second
            currentCropPercentages = Pair(originalHeightCropPercentage / 2, originalWidthCropPercentage)
        }

        val (widthCropPercent, heightCropPercent) = currentCropPercentages
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
            else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
        }

        cropRect.inset(
            (imageWidth * widthCrop / 2).toInt(),
            (imageHeight * heightCrop / 2).toInt()
        )

        val croppedBitmap = ImageUtils.rotateAndCrop(bitmap, rotationDegrees, cropRect)
            ?: return Result.failure(Exception("Failed to crop and rotate bitmap"))

        val text = detector.process(InputImage.fromBitmap(croppedBitmap, 0)).await()
        if (text.textBlocks.isEmpty()) {
            return Result.failure(Exception("No detected text blocks"))
        }

        val textBlock = text.textBlocks.firstOrNull { it.text.all { char -> char.isDigit() } }
            ?: return Result.failure(Exception("No digit text blocks found"))

        val boundingBox = textBlock.boundingBox ?: return Result.failure(Exception("No bounding box found"))
        val croppedRatio = Float2(
            boundingBox.centerX() / croppedBitmap.width.toFloat(),
            boundingBox.centerY() / croppedBitmap.height.toFloat()
        )

        val x = displaySize.first * croppedRatio.x
        val y = displaySize.second * croppedRatio.y

        return Result.success(
            DetectedObjectResult(
                label = textBlock.text,
                centerCoordinate = Float2(x, y)
            )
        )
    }
}
