package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.MidnightBlack
import java.util.Random
import kotlin.math.sin

@Composable
fun AuraCanvas(
    modifier: Modifier = Modifier,
    imageUrl: String, // "preset_1", "preset_2", or "UriString"
    filterType: String,
    filterIntensity: Float = 1.0f,
    brightness: Float = 0.0f,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    blur: Float = 0.0f,
    vignette: Float = 0.0f,
    grain: Float = 0.0f,
    activeOverlay: String = "NONE", // NONE, DUST, LIGHT_LEAK, SPARKLE
    overlayOpacity: Float = 0.5f,
    seed: Long = 42L // Deterministic random seed for old-school film sparks
) {
    val context = LocalContext.current
    
    // Load and cash the ImageBitmap based on resource or local URI
    val imageBitmapState = remember(imageUrl) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(imageUrl) {
        try {
            if (imageUrl == "preset_1") {
                val b = BitmapFactory.decodeResource(context.resources, R.drawable.preset_model_1)
                imageBitmapState.value = b?.asImageBitmap()
            } else if (imageUrl == "preset_2") {
                val b = BitmapFactory.decodeResource(context.resources, R.drawable.preset_model_2)
                imageBitmapState.value = b?.asImageBitmap()
            } else if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                val uri = Uri.parse(imageUrl)
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                imageBitmapState.value = bitmap?.copy(Bitmap.Config.ARGB_8888, true)?.asImageBitmap()
            } else {
                // Fallback to logo / default
                val b = BitmapFactory.decodeResource(context.resources, R.drawable.ic_aura_logo)
                imageBitmapState.value = b?.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Ultimate fallback to loading raw logo
            try {
                val b = BitmapFactory.decodeResource(context.resources, R.drawable.ic_aura_logo)
                imageBitmapState.value = b?.asImageBitmap()
            } catch (ex: Exception) {
                // Noop
            }
        }
    }

    val finalImage = imageBitmapState.value

    // Smooth fade-in transition when filter changes
    val animAlpha = remember { Animatable(1f) }
    LaunchedEffect(filterType) {
        animAlpha.snapTo(0f)
        animAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.background(MidnightBlack),
        contentAlignment = Alignment.Center
    ) {
        if (finalImage != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = animAlpha.value }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val imageWidth = finalImage.width.toFloat()
                val imageHeight = finalImage.height.toFloat()

                // Calculate Aspect Ratio Fit inside the canvas container
                val scale = kotlin.math.min(canvasWidth / imageWidth, canvasHeight / imageHeight)
                val destWidth = imageWidth * scale
                val destHeight = imageHeight * scale
                val dx = (canvasWidth - destWidth) / 2f
                val dy = (canvasHeight - destHeight) / 2f

                val destSize = Size(destWidth, destHeight)
                val destOffset = Offset(dx, dy)

                // 1. Setup composite Paint adjustments (brightness, contrast, saturation, filter base)
                val compositeMatrix = AuraFilterEngine.getCompositeMatrix(
                    filterType = filterType,
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation
                )

                // Linear interpolation with clean state to support filter intensity sliders
                val finalFilter = ColorFilter.colorMatrix(compositeMatrix)

                // 2. Draw Image Canvas
                if ((filterType == "GLITCH" || filterType == "D3D") && filterIntensity > 0.1f) {
                    // Draw RGB chromatic splits
                    val splitOffset = 12f * filterIntensity
                    
                    // Cyan-shifted overlay
                    val cyanMatrix = ColorMatrix(floatArrayOf(
                        0f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f, 0f, 0f,
                        0f, 0f, 0f, 0.7f, 0f
                    ))
                    
                    // Red-shifted overlay
                    val redMatrix = ColorMatrix(floatArrayOf(
                        1f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0.7f, 0f
                    ))

                    // First channel (Cyan, offset left)
                    drawImage(
                        image = finalImage,
                        dstOffset = IntOffset((dx - splitOffset).toInt(), dy.toInt()),
                        dstSize = IntSize(destWidth.toInt(), destHeight.toInt()),
                        colorFilter = ColorFilter.colorMatrix(cyanMatrix)
                    )

                    // Second channel (Red, offset right)
                    drawImage(
                        image = finalImage,
                        dstOffset = IntOffset((dx + splitOffset).toInt(), dy.toInt()),
                        dstSize = IntSize(destWidth.toInt(), destHeight.toInt()),
                        colorFilter = ColorFilter.colorMatrix(redMatrix)
                    )

                    // Core Blend
                    drawImage(
                        image = finalImage,
                        dstOffset = IntOffset(dx.toInt(), dy.toInt()),
                        dstSize = IntSize(destWidth.toInt(), destHeight.toInt()),
                        colorFilter = finalFilter,
                        alpha = 0.6f
                    )
                } else {
                    // Standard single draw pass
                    drawImage(
                        image = finalImage,
                        dstOffset = IntOffset(dx.toInt(), dy.toInt()),
                        dstSize = IntSize(destWidth.toInt(), destHeight.toInt()),
                        colorFilter = finalFilter
                    )
                }

                // 3. Draw Scanlines Overlay (VHS/Glitch)
                if ((filterType == "VHS" || filterType == "GLITCH")) {
                    val scanlineSpacing = 6f
                    val lineCount = (destHeight / scanlineSpacing).toInt()
                    for (i in 0 until lineCount) {
                        val y = dy + (i * scanlineSpacing)
                        drawLine(
                            color = Color.Black.copy(alpha = 0.12f),
                            start = Offset(dx, y),
                            end = Offset(dx + destWidth, y),
                            strokeWidth = 2f
                        )
                    }
                    // Vintage horizontal VHS static glitch bar
                    val randomBarY = dy + (destHeight * 0.7f)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.0f)
                            )
                        ),
                        topLeft = Offset(dx, randomBarY),
                        size = Size(destWidth, 24f)
                    )
                }

                // 4. Overlays (DUST, LIGHT_LEAK, SPARKLE)
                if (activeOverlay == "DUST" && overlayOpacity > 0.05f) {
                    val random = Random(seed)
                    // Draw random dust spots and tiny hair outlines
                    val dustCount = 15
                    for (i in 0 until dustCount) {
                        val rx = dx + random.nextFloat() * destWidth
                        val ry = dy + random.nextFloat() * destHeight
                        val radius = random.nextFloat() * 1.5f + 0.5f
                        val isWhite = random.nextBoolean()
                        drawCircle(
                            color = if (isWhite) Color.White.copy(alpha = overlayOpacity * 0.7f) else Color.Black.copy(alpha = overlayOpacity * 0.7f),
                            radius = radius,
                            center = Offset(rx, ry)
                        )
                    }
                    // Draw a couple of retro film hair scratches
                    val scratchCount = 3
                    for (i in 0 until scratchCount) {
                        val sx = dx + random.nextFloat() * destWidth
                        val sy = dy + random.nextFloat() * destHeight
                        val sLength = random.nextFloat() * 40f + 10f
                        val angle = random.nextFloat() * 6.28f
                        val ex = sx + sLength * kotlin.math.cos(angle)
                        val ey = sy + sLength * sin(angle)
                        drawLine(
                            color = Color(0xFFF7F3E5).copy(alpha = overlayOpacity * 0.5f),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 1f
                        )
                    }
                } else if (activeOverlay == "LIGHT_LEAK" && overlayOpacity > 0.05f) {
                    // Top-Left Retro Orange leak
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF4E00).copy(alpha = overlayOpacity * 0.7f),
                                Color(0xFFFF9E00).copy(alpha = overlayOpacity * 0.3f),
                                Color.Transparent
                            ),
                            center = Offset(dx, dy),
                            radius = destWidth * 0.5f
                        ),
                        center = Offset(dx, dy),
                        radius = destWidth * 0.5f
                    )
                    // Right edge pink streak
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFE01E37).copy(alpha = overlayOpacity * 0.1f),
                                Color(0xFFFF0A54).copy(alpha = overlayOpacity * 0.5f)
                            ),
                            startX = dx + destWidth * 0.6f,
                            endX = dx + destWidth
                        ),
                        topLeft = Offset(dx + destWidth * 0.6f, dy),
                        size = Size(destWidth * 0.4f, destHeight)
                    )
                } else if (activeOverlay == "SPARKLE" && overlayOpacity > 0.05f) {
                    val sparkleRandom = Random(seed + 1)
                    val sparkleCount = 6
                    for (i in 0 until sparkleCount) {
                        // High contrast points simulator
                        val sx = dx + sparkleRandom.nextFloat() * destWidth
                        val sy = dy + sparkleRandom.nextFloat() * destHeight
                        val sizeRatio = (sparkleRandom.nextFloat() * 12f + 8f) * overlayOpacity
                        
                        // Draw horizontal glossy shine bar
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(sx - sizeRatio * 1.5f, sy),
                            end = Offset(sx + sizeRatio * 1.5f, sy),
                            strokeWidth = 2.5f
                        )
                        // Vertical glossy bar
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(sx, sy - sizeRatio * 1.5f),
                            end = Offset(sx, sy + sizeRatio * 1.5f),
                            strokeWidth = 2.5f
                        )
                        // Inner core aura glow
                        drawCircle(
                            color = Color(0xFFFFEFA6).copy(alpha = 0.6f),
                            radius = sizeRatio * 0.6f,
                            center = Offset(sx, sy)
                        )
                    }
                }

                // 5. Draw grain specks on top if intensity set
                if (grain > 0.05f) {
                    val grainRandom = Random()
                    val gCount = (destWidth * destHeight * 0.015f * grain).toInt()
                    for (i in 0 until gCount) {
                        val gx = dx + grainRandom.nextFloat() * destWidth
                        val gy = dy + grainRandom.nextFloat() * destHeight
                        val opacity = grainRandom.nextFloat() * 0.22f * grain
                        drawRect(
                            color = Color.White.copy(alpha = opacity),
                            topLeft = Offset(gx, gy),
                            size = Size(1.5f, 1.5f)
                        )
                    }
                }

                // 6. Draw vignette radial ring shading
                if (vignette > 0.05f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = vignette * 0.35f),
                                Color.Black.copy(alpha = vignette * 0.85f)
                            ),
                            center = Offset(dx + destWidth / 2f, dy + destHeight / 2f),
                            radius = destHeight * 0.72f
                        ),
                        topLeft = Offset(dx, dy),
                        size = destSize
                    )
                }
            }
        } else {
            // Elegant placeholder spinner until loaded
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = CyberCyan
                )
            }
        }
    }
}
