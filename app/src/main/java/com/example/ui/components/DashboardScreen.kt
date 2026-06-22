package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Draft
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drafts by viewModel.allDrafts.collectAsState()

    // ActivityResultLauncher for picking images from Device Gallery to start editing
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.startNewDraft(uri.toString())
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightBlack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { galleryLauncher.launch("image/*") },
                containerColor = NeonPurple,
                contentColor = Color.Black,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Add, "Import Photo", tint = Color.Black) },
                text = { Text("IMPORT PHOTO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 11.sp) },
                modifier = Modifier.testTag("import_photo_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. App Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AURA",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = NeonPurple,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "PRESTIGE RETRO EDITOR",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    
                    // Stylish Logo Container
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MidnightSurfaceCard)
                            .padding(2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_aura_logo),
                            contentDescription = "Aura App Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 2. Creative Preset Templates Roll (Swipe to start editing!)
            item {
                Column {
                    Text(
                        text = "PRESET TEMPLATES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextGray,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        // Preset 1 Card
                        item {
                            PresetCard(
                                title = "Cyber Neon Model",
                                drawableRes = R.drawable.preset_model_1,
                                onClick = { viewModel.startNewDraft("preset_1") },
                                tag = "preset_neon_card"
                            )
                        }
                        
                        // Preset 2 Card
                        item {
                            PresetCard(
                                title = "VHS Tokyo Night",
                                drawableRes = R.drawable.preset_model_2,
                                onClick = { viewModel.startNewDraft("preset_2") },
                                tag = "preset_vhs_card"
                            )
                        }

                        // Preset 3 (Blank Canvas) Card
                        item {
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MidnightSurfaceCard)
                                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Custom Upload",
                                        tint = NeonPurple,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "IMPORT CUSTOM",
                                        fontSize = 11.sp,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. User saved drafts projects section
            item {
                Text(
                    text = "MY SAVED DRAFTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextGray,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (drafts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                            .background(MidnightSurfaceCard, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderSlate.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                            contentDescription = "Empty Drafts",
                            tint = TextGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your aesthetic space is clear",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap a template above or import your own photo to build retro film layouts.",
                            fontSize = 12.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                items(drafts, key = { it.id }) { draft ->
                    DraftItemRow(
                        draft = draft,
                        onClick = { viewModel.editDraft(draft) },
                        onDelete = { viewModel.deleteDraft(draft) }
                    )
                }
            }
            
            // 4. About & Developer Showcase Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .border(1.dp, BorderSlate, RoundedCornerShape(20.dp))
                        .testTag("about_developer_card"),
                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header with Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "System Info",
                                tint = NeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "NEXVOICE OS & DEVELOPER CENTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.5.sp
                            )
                        }

                        // Divider line
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSlate))

                        // 1. About Developer
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ABOUT DEVELOPER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Prince AR Abdur Rahman",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Contact WhatsApp: 01707424006 | 01796951709",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Facebook: https://www.facebook.com/share/1BNn32qoJo/\nInstagram: https://www.instagram.com/ur___abdur____rahman__2008",
                                fontSize = 10.sp,
                                color = CyberCyan,
                                lineHeight = 14.sp
                            )
                        }

                        // Divider line
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSlate.copy(alpha = 0.5f)))

                        // 2. About Company
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ABOUT COMPANY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "NexVora Lab's Ofc",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextWhite,
                                lineHeight = 15.sp
                            )
                        }

                        // Divider line
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSlate.copy(alpha = 0.5f)))

                        // 3. System & OS Technical Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NEXVOICE OS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextWhite,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Technical Information",
                                    fontSize = 10.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                text = "Version 1.0.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Divider line
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSlate.copy(alpha = 0.5f)))

                        // 4. Credits
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "CREDITS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Developed by Prince AR Abdur Rahman",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = "Published by NexVora Lab's Ofc",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextGray.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding to stay clear of FAB
            }
        }
    }
}

@Composable
fun PresetCard(
    title: String,
    drawableRes: Int,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Vignette shading gradient at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 180f
                        )
                    )
            )
            // Title Overlay
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
fun DraftItemRow(
    draft: Draft,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMMM dd, hh:mm a", Locale.getDefault()) }
    val dateString = formatter.format(Date(draft.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MidnightSurfaceCard)
            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("draft_item_${draft.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail view
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MidnightBlack)
        ) {
            val painter = when (draft.imageUrl) {
                "preset_1" -> painterResource(id = R.drawable.preset_model_1)
                "preset_2" -> painterResource(id = R.drawable.preset_model_2)
                else -> painterResource(id = R.drawable.ic_aura_logo) // Logo fallback for drafts
            }
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // If there's an active filter, show a small badge with its name
            if (draft.filterType != "NONE") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(AccentMagenta)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = draft.filterType,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = draft.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateString,
                fontSize = 11.sp,
                color = TextGray
            )
        }

        // Quick delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_draft_${draft.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete project",
                tint = TextGray.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
