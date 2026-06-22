package com.example.ui.components

import androidx.compose.ui.graphics.ColorMatrix

object AuraFilterEngine {

    // Generates a composite ColorMatrix based on selected filter and user adjustments
    fun getCompositeMatrix(
        filterType: String,
        brightness: Float, // -1f to 1f
        contrast: Float,   // 0f to 2f
        saturation: Float  // 0f to 2f
    ): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Base Filter Tone
        val filterMatrix = when (filterType) {
            "VHS" -> ColorMatrix(floatArrayOf(
                1.1f, 0.0f, 0.0f, 0.0f, 10f,
                0.0f, 1.0f, 0.0f, 0.0f, 5f,
                0.0f, 0.0f, 0.9f, 0.0f, -10f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            "GLITCH" -> ColorMatrix(floatArrayOf(
                0.9f, 0.1f, 0.1f, 0.0f, 15f,
                0.0f, 1.1f, 0.0f, 0.0f, -5f,
                0.1f, 0.0f, 1.2f, 0.0f, 10f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            "DISCO" -> ColorMatrix(floatArrayOf(
                1.3f, 0.0f, 0.2f, 0.0f, 20f,
                0.1f, 0.8f, 0.1f, 0.0f, -10f,
                0.3f, 0.0f, 1.4f, 0.0f, 25f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            "D3D" -> ColorMatrix(floatArrayOf(
                1.2f, 0.0f, 0.0f, 0.0f, 20f,
                0.0f, 0.8f, 0.1f, 0.0f, -10f,
                0.0f, 0.1f, 1.3f, 0.0f, 20f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            "NEON" -> ColorMatrix(floatArrayOf(
                1.1f, 0.0f, 0.3f, 0.0f, 15f,
                0.0f, 0.9f, 0.2f, 0.0f, 10f,
                0.4f, 0.0f, 1.3f, 0.0f, 30f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            "MIAMI" -> ColorMatrix(floatArrayOf(
                1.2f, 0.1f, 0.0f, 0.0f, 15f,
                0.0f, 1.0f, 0.2f, 0.0f, -5f,
                0.2f, 0.0f, 1.3f, 0.0f, 15f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            "GOLD" -> ColorMatrix(floatArrayOf(
                1.3f, 0.0f, 0.0f, 0.0f, 25f,
                0.1f, 1.1f, 0.0f, 0.0f, 15f,
                0.0f, 0.0f, 0.7f, 0.0f, -15f,
                0.0f, 0.0f, 0.0f, 1.0f, 0f
            ))
            else -> ColorMatrix() // NONE
        }
        
        // Multiply matrices
        // 2. Brightness Matrix (offsets in 5th item of each row)
        val brightMat = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightness * 255f,
            0f, 1f, 0f, 0f, brightness * 255f,
            0f, 0f, 1f, 0f, brightness * 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        // 3. Contrast Matrix
        // Contrast scales around a midpoint of 0.5 (or luminance of 128)
        val t = (1.0f - contrast) * 0.5f * 255f
        val contMat = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, t,
            0f, contrast, 0f, 0f, t,
            0f, 0f, contrast, 0f, t,
            0f, 0f, 0f, 1f, 0f
        ))

        // 4. Saturation Matrix
        // Standard luminance weights for grayscale conversion: r=0.213, g=0.715, b=0.072
        val r = 0.213f * (1f - saturation)
        val g = 0.715f * (1f - saturation)
        val b = 0.072f * (1f - saturation)
        val satMat = ColorMatrix(floatArrayOf(
            r + saturation, g, b, 0f, 0f,
            r, g + saturation, b, 0f, 0f,
            r, g, b + saturation, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // Concat operations
        // Combine matrix multiplications
        concat(matrix, filterMatrix)
        concat(matrix, brightMat)
        concat(matrix, contMat)
        concat(matrix, satMat)

        return matrix
    }

    private fun concat(target: ColorMatrix, other: ColorMatrix) {
        val t = target.values
        val o = other.values
        val result = FloatArray(20)

        for (row in 0 until 4) {
            val rIdx = row * 5
            for (col in 0 until 5) {
                var sum = 0f
                for (k in 0 until 4) {
                    sum += o[rIdx + k] * t[k * 5 + col]
                }
                if (col == 4) {
                    // Add offset translation
                    sum += o[rIdx + 4]
                }
                result[rIdx + col] = sum
            }
        }
        
        for (i in 0 until 20) {
            t[i] = result[i]
        }
    }
}
