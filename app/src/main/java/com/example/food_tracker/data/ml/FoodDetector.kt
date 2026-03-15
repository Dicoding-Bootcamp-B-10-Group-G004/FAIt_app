package com.example.food_tracker.data.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

class FoodDetector(context: Context) {

    private val interpreter: Interpreter

    private val labels = listOf(
        "ayam bakar",
        "ayam betutu",
        "ayam goreng",
        "ayam pop",
        "bakso",
        "coto makassar",
        "gado gado",
        "gudeg",
        "nasi goreng",
        "pempek",
        "rawon",
        "rendang",
        "sate madura",
        "sate padang",
        "soto"
    )

    init {

        val model = context.assets.open("best_float32.tflite").readBytes()

        val buffer = ByteBuffer.allocateDirect(model.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(model)

        interpreter = Interpreter(buffer)
    }

    fun detect(bitmap: Bitmap): String {

        val inputSize = 640

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val inputBuffer =
            ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)

        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {

                val pixel = resized.getPixel(x, y)

                inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
                inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
                inputBuffer.putFloat((pixel and 0xFF) / 255f)
            }
        }

        val output = Array(1) { Array(25200) { FloatArray(6) } }

        interpreter.run(inputBuffer, output)

        var bestScore = 0f
        var bestClass = -1

        for (box in output[0]) {

            val confidence = box[4]
            val classId = box[5].toInt()

            if (confidence > bestScore) {
                bestScore = confidence
                bestClass = classId
            }
        }

        return if (bestClass != -1) labels[bestClass] else "Unknown Food"
    }
}