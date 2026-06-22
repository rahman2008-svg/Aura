package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DashboardScreen
import com.example.ui.components.EditorScreen
import com.example.ui.theme.AuraTheme
import com.example.ui.viewmodel.AuraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable Edge-to-Edge full render layout
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                // Instantiate our centralized ViewModel safely
                val viewModel: AuraViewModel = viewModel()
                
                // Track active editor draft to handle layout state transitions
                val currentDraft by viewModel.currentDraft.collectAsState()

                if (currentDraft == null) {
                    DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    EditorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
