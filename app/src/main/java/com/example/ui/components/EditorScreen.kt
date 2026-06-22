package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Collect editor values reactively from ViewModel
    val currentDraft by viewModel.currentDraft.collectAsState()
    val imageUrl by viewModel.activeImageUrl.collectAsState()
    val filterType by viewModel.activeFilterType.collectAsState()
    val filterIntensity by viewModel.activeIntensity.collectAsState()
    val brightness by viewModel.activeBrightness.collectAsState()
    val contrast by viewModel.activeContrast.collectAsState()
    val saturation by viewModel.activeSaturation.collectAsState()
    val vignette by viewModel.activeVignette.collectAsState()
    val grain by viewModel.activeGrain.collectAsState()
    val activeOverlay by viewModel.activeOverlay.collectAsState()
    val overlayOpacity by viewModel.activeOverlayOpacity.collectAsState()

    // Local UI states
    var projectTitle by remember(currentDraft) { mutableStateOf(currentDraft?.title ?: "Aesthetic Project") }
    var activeTab by remember { mutableStateOf("FILTERS") } // FILTERS, OVERLAYS, ADJUST, MUSIC
    var selectedAdjustType by remember { mutableStateOf("BRIGHTNESS") } // BRIGHTNESS, CONTRAST, SATURATION, VIGNETTE, GRAIN
    
    // Playback state of music overlays
    var activeMusicTrack by remember { mutableStateOf<String?>(null) }
    var isSavingInProgress by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }

    // Seed state to allow manual "re-shuffling" of random film dust
    var dustSeed by remember { mutableStateOf(42L) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.activeImageUrl.value = uri.toString()
        }
    }

    if (currentDraft == null) return

    // Screen shell
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Control Bar (Navigate / Rename / Save / Export)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button (Styled as Close as per Prestige design)
                IconButton(
                    onClick = { viewModel.exitEditor() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MidnightSurfaceCard)
                        .testTag("editor_back_btn")
                ) {
                    Icon(
                        imageVector = org.jetbrains.annotations.Nullable::class.run { Icons.Default.Close },
                        contentDescription = "Exit workspace",
                        tint = Color.White
                    )
                }

                // Title string inline editable styled with Bold Prestige Typography
                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = { 
                        projectTitle = it
                        viewModel.saveDraft(it) // Autosave title updates!
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .height(52.dp)
                        .testTag("editor_title_input"),
                    textStyle = LocalTextStyle.current.copy(
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MidnightSurfaceCard,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = BorderSlate,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextGray
                    )
                )

                // Save Disk icon & Export sharing buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save to database
                    Button(
                        onClick = {
                            viewModel.saveDraft(projectTitle)
                            Toast.makeText(context, "Project Draft Saved Offline! 💾", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderSlate, contentColor = TextWhite),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("save_draft_btn")
                    ) {
                        Text(
                            text = "SAVE DRAFT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextWhite
                        )
                    }

                    // Export confirmation button (Pill-shaped, theme colors, bold uppercase)
                    Button(
                        onClick = {
                            isSavingInProgress = true
                            coroutineScope.launch {
                                // Simulate rendering time of the Prequel effects
                                delay(2200)
                                isSavingInProgress = false
                                showExportSuccessDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.Black),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("export_artwork_btn")
                    ) {
                        Text(
                            text = "EXPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            // 2. Central Immersive Canvas Viewer Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AuraCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(40.dp))
                        .border(1.dp, BorderSlate, RoundedCornerShape(40.dp))
                        .testTag("render_canvas"),
                    imageUrl = imageUrl,
                    filterType = filterType,
                    filterIntensity = filterIntensity,
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    vignette = vignette,
                    grain = grain,
                    activeOverlay = activeOverlay,
                    overlayOpacity = overlayOpacity,
                    seed = dustSeed
                )

                // Floating AI Enhancement Badge Overlay from Prestige design HTML
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Active",
                            tint = NeonPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "AI ENHANCEMENT ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }

                // Overlay side tools floating bar from Prestige design HTML layout pattern
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.40f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick Select Photo
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { galleryLauncher.launch("image/*") }
                            .padding(4.dp)
                            .testTag("select_photo_sidebar_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Select Photo",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick Seed Shuffle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { dustSeed = (1L..1000L).random() }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Shuffle",
                            tint = NeonPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Track play visual equalizer indicator
                if (activeMusicTrack != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = activeMusicTrack!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Quick mock EQ graphic generator
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.height(10.dp)
                            ) {
                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.8f).background(Color.White))
                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.4f).background(Color.White))
                                Box(modifier = Modifier.width(2.dp).fillMaxHeight(0.9f).background(Color.White))
                            }
                        }
                    }
                }
            }

            // 3. Command Tab Controller Panel (Bottom)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MidnightSurface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 16.dp)
                ) {
                    
                    // Variable value adjustment slider bar (renders conditionally!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .heightIn(min = 56.dp, max = 240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (activeTab) {
                            "FILTERS" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // SELECT PHOTO action button under the Presets Workspace
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "FILTER PROFILE MIX",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextGray,
                                            letterSpacing = 1.sp
                                        )
                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = BorderSlate, contentColor = TextWhite),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("select_photo_gallery_btn")
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = "Select", modifier = Modifier.size(12.dp), tint = CyberCyan)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("SELECT PHOTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        }
                                    }

                                    if (filterType != "NONE") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Filter Mix Strength", fontSize = 11.sp, color = TextGray)
                                            Text("${(filterIntensity * 100).toInt()}%", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                                        }
                                        Slider(
                                            value = filterIntensity,
                                            onValueChange = { viewModel.activeIntensity.value = it },
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberCyan,
                                                activeTrackColor = CyberCyan,
                                                inactiveTrackColor = BorderSlate
                                            ),
                                            modifier = Modifier.testTag("filter_intensity_slider")
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Select a filter profile below to apply retro grading", fontSize = 11.sp, color = TextGray, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                            "OVERLAYS" -> {
                                if (activeOverlay != "NONE") {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Overlay Blend Opacity", fontSize = 11.sp, color = TextGray)
                                            Text("${(overlayOpacity * 100).toInt()}%", fontSize = 11.sp, color = AccentMagenta, fontWeight = FontWeight.Bold)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Slider(
                                                value = overlayOpacity,
                                                onValueChange = { viewModel.activeOverlayOpacity.value = it },
                                                colors = SliderDefaults.colors(
                                                    thumbColor = AccentMagenta,
                                                    activeTrackColor = AccentMagenta,
                                                    inactiveTrackColor = BorderSlate
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("overlay_opacity_slider")
                                            )
                                            if (activeOverlay == "DUST") {
                                                // Reshuffle dust particle locations button
                                                IconButton(
                                                    onClick = { dustSeed = System.currentTimeMillis() },
                                                    modifier = Modifier.padding(start = 8.dp)
                                                ) {
                                                    Icon(Icons.Default.Refresh, "Reshuffle specks", tint = TextWhite)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text("Choose a film layer: old fibers, warm flares or glowing sparkles.", fontSize = 12.sp, color = TextGray, textAlign = TextAlign.Center)
                                }
                            }
                            "ADJUST" -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // 1. Brightness
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("BRIGHTNESS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextGray, modifier = Modifier.width(90.dp))
                                        Slider(
                                            value = brightness,
                                            valueRange = -0.5f..0.5f,
                                            onValueChange = { viewModel.activeBrightness.value = it },
                                            colors = SliderDefaults.colors(
                                                thumbColor = NeonPurple,
                                                activeTrackColor = NeonPurple,
                                                inactiveTrackColor = BorderSlate
                                            ),
                                            modifier = Modifier.weight(1f).testTag("brightness_slider")
                                        )
                                        Text("${(brightness * 200).toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                                    }
                                    
                                    // 2. Contrast
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("CONTRAST", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextGray, modifier = Modifier.width(90.dp))
                                        Slider(
                                            value = contrast,
                                            valueRange = 0.0f..2.0f,
                                            onValueChange = { viewModel.activeContrast.value = it },
                                            colors = SliderDefaults.colors(
                                                thumbColor = NeonPurple,
                                                activeTrackColor = NeonPurple,
                                                inactiveTrackColor = BorderSlate
                                            ),
                                            modifier = Modifier.weight(1f).testTag("contrast_slider")
                                        )
                                        Text("${(contrast * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                                    }

                                    // 3. Saturation
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("SATURATION", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextGray, modifier = Modifier.width(90.dp))
                                        Slider(
                                            value = saturation,
                                            valueRange = 0.0f..2.0f,
                                            onValueChange = { viewModel.activeSaturation.value = it },
                                            colors = SliderDefaults.colors(
                                                thumbColor = NeonPurple,
                                                activeTrackColor = NeonPurple,
                                                inactiveTrackColor = BorderSlate
                                            ),
                                            modifier = Modifier.weight(1f).testTag("saturation_slider")
                                        )
                                        Text("${(saturation * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                                    }
                                }
                            }
                            "MUSIC" -> {
                                Text("Aesthetic preset background atmospheres. Synced with canvas overlay speed.", fontSize = 12.sp, color = TextGray, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // horizontal action panel slider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        when (activeTab) {
                            "FILTERS" -> {
                                val filterCategories = listOf("NONE", "VHS", "GLITCH", "DISCO", "D3D", "NEON", "MIAMI", "GOLD")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(filterCategories) { filter ->
                                        FilterButton(
                                            name = filter,
                                            isSelected = filterType == filter,
                                            onClick = { viewModel.activeFilterType.value = filter }
                                        )
                                    }
                                }
                            }
                            "OVERLAYS" -> {
                                val overlays = listOf("NONE", "DUST", "LIGHT_LEAK", "SPARKLE")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(overlays) { overlayItem ->
                                        OverlayButton(
                                            name = overlayItem,
                                            isSelected = activeOverlay == overlayItem,
                                            onClick = { viewModel.activeOverlay.value = overlayItem }
                                        )
                                    }
                                }
                            }
                            "ADJUST" -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "USE SLIDERS TO FINE-TUNE RETRO COLOR PARAMETERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextGray,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            "MUSIC" -> {
                                val tracks = listOf("NONE", "RETRO WAVE", "LO-FI SUNSET", "VAPORWAVE BEAT", "NEON CYBER")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(tracks) { track ->
                                        MusicPresetButton(
                                            trackName = track,
                                            isSelected = activeMusicTrack == (if (track == "NONE") null else track),
                                            onClick = {
                                                activeMusicTrack = if (track == "NONE") null else track
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Tab selection row: PRESETS | FILTERS | ADJUST | EFFECTS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabTrigger(title = "PRESETS", isSelected = activeTab == "FILTERS", onClick = { activeTab = "FILTERS" })
                        TabTrigger(title = "FILTERS", isSelected = activeTab == "OVERLAYS", onClick = { activeTab = "OVERLAYS" })
                        TabTrigger(title = "ADJUST", isSelected = activeTab == "ADJUST", onClick = { activeTab = "ADJUST" })
                        TabTrigger(title = "EFFECTS", isSelected = activeTab == "MUSIC", onClick = { activeTab = "MUSIC" })
                    }
                }
            }
        }

        // Overlay dialog for saving compilation time
        if (isSavingInProgress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = CyberCyan,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "AURA ARTWORK ENGINE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rendering cinematic retro elements...",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }

        // Successfully saved / exported modal
        if (showExportSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showExportSuccessDialog = false },
                containerColor = MidnightSurface,
                title = {
                    Text(
                        text = "Creation Polished! ✨",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Export success",
                                tint = CyberCyan,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Project \"$projectTitle\" has been rendered at high definition and saved successfully to your offline gallery storage system!",
                            fontSize = 13.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showExportSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!", color = MidnightBlack, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// ---------------- Support Styling Composable Items -------------------

@Composable
fun TabTrigger(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) NeonPurple else TextGray.copy(alpha = 0.5f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("tab_btn_$title"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun FilterButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) CyberCyan else BorderSlate
    val textColor = if (isSelected) Color.White else TextGray
    Box(
        modifier = Modifier
            .width(88.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MidnightSurfaceCard else Color.Transparent)
            .border(2.dp, tint, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("filter_option_$name"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (name) {
                            "NONE" -> Brush.radialGradient(listOf(Color.White, Color.Black))
                            "VHS" -> Brush.linearGradient(listOf(Color(0xFFE2D6FF), Color(0xFFC0A6FF)))
                            "GLITCH" -> Brush.linearGradient(listOf(Color(0xFF00FFF0), Color(0xFFFF00C8)))
                            "DISCO" -> Brush.sweepGradient(listOf(Color(0xFFFF0A54), Color(0xFFEE40F1)))
                            "D3D" -> Brush.horizontalGradient(listOf(Color(0xFFFF5462), Color(0xFF54FFF9)))
                            "NEON" -> Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF2B0061)))
                            "MIAMI" -> Brush.linearGradient(listOf(Color(0xFFFF8B94), Color(0xFF4AC4FF)))
                            else -> Brush.sweepGradient(listOf(Color(0xFFFFD000), Color(0xFFFFA200)))
                        }
                    )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun OverlayButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) AccentMagenta else BorderSlate
    val textColor = if (isSelected) Color.White else TextGray
    Box(
        modifier = Modifier
            .width(88.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MidnightSurfaceCard else Color.Transparent)
            .border(2.dp, tint, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("overlay_option_$name"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (name) {
                    "NONE" -> Icons.Default.LayersClear
                    "DUST" -> Icons.Default.Grain
                    "LIGHT_LEAK" -> Icons.Default.BrightnessHigh
                    else -> Icons.Default.AutoAwesome
                },
                contentDescription = name,
                tint = if (isSelected) AccentMagenta else TextGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name.replace("_", " "),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class AdjustOption(val key: String, val icon: ImageVector)

@Composable
fun AdjustButton(
    option: AdjustOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) AuraGold else BorderSlate
    val textAndIconColor = if (isSelected) Color.White else TextGray
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MidnightSurfaceCard else Color.Transparent)
            .border(2.dp, tint, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("adjust_option_${option.key}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.key,
                tint = if (isSelected) AuraGold else TextGray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = option.key.take(5),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textAndIconColor
            )
        }
    }
}

@Composable
fun MusicPresetButton(
    trackName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) CyberCyan else BorderSlate
    val iconColor = if (isSelected) CyberCyan else TextGray
    Box(
        modifier = Modifier
            .width(96.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MidnightSurfaceCard else Color.Transparent)
            .border(2.dp, tint, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("music_option_$trackName"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (trackName == "NONE") Icons.Default.MusicOff else Icons.Default.MusicNote,
                contentDescription = trackName,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trackName,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
