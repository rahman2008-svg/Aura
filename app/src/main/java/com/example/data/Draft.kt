package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imageUrl: String, // Can be "preset_1", "preset_2", etc. or a content URI
    val filterType: String = "NONE", // NONE, VHS, GLITCH, DISCO, D3D, NEON, MIAMI, GOLD
    val filterIntensity: Float = 1.0f,
    val brightness: Float = 0.0f, // -1.0f to 1.0f
    val contrast: Float = 1.0f,   // 0.0f to 2.0f
    val saturation: Float = 1.0f, // 0.0f to 2.0f
    val blur: Float = 0.0f,       // 0.0f to 1.0f
    val vignette: Float = 0.0f,   // 0.0f to 1.0f
    val grain: Float = 0.0f,      // 0.0f to 1.0f
    val activeOverlay: String = "NONE", // NONE, DUST, LIGHT_LEAK, SPARKLE
    val overlayOpacity: Float = 0.5f,
    val timestamp: Long = System.currentTimeMillis()
)
