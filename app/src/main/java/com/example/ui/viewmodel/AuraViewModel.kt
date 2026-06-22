package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Draft
import com.example.data.DraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val draftDao = AppDatabase.getDatabase(application).draftDao()
    private val repository = DraftRepository(draftDao)

    // Flow reflecting all offline saved draft projects
    val allDrafts: StateFlow<List<Draft>> = repository.allDrafts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Editor navigation and state machine
    private val _currentDraft = MutableStateFlow<Draft?>(null)
    val currentDraft: StateFlow<Draft?> = _currentDraft.asStateFlow()

    // Temporary active editor parameters
    var activeImageUrl = MutableStateFlow("preset_1")
    var activeFilterType = MutableStateFlow("NONE")
    var activeIntensity = MutableStateFlow(1.0f)
    var activeBrightness = MutableStateFlow(0.0f)
    var activeContrast = MutableStateFlow(1.0f)
    var activeSaturation = MutableStateFlow(1.0f)
    var activeVignette = MutableStateFlow(0.0f)
    var activeGrain = MutableStateFlow(0.0f)
    var activeOverlay = MutableStateFlow("NONE")
    var activeOverlayOpacity = MutableStateFlow(0.5f)

    // Initiates editing a brand new project with a preset photo template
    fun startNewDraft(imageUrl: String = "preset_1") {
        val newDraft = Draft(
            id = 0, // 0 triggers Room auto-generation of ID
            title = "Aesthetic Project",
            imageUrl = imageUrl
        )
        _currentDraft.value = newDraft
        loadEditorParameters(newDraft)
    }

    // Opens an existing saved draft project
    fun editDraft(draft: Draft) {
        _currentDraft.value = draft
        loadEditorParameters(draft)
    }

    // Restores edit slider values from project model
    private fun loadEditorParameters(draft: Draft) {
        activeImageUrl.value = draft.imageUrl
        activeFilterType.value = draft.filterType
        activeIntensity.value = draft.filterIntensity
        activeBrightness.value = draft.brightness
        activeContrast.value = draft.contrast
        activeSaturation.value = draft.saturation
        activeVignette.value = draft.vignette
        activeGrain.value = draft.grain
        activeOverlay.value = draft.activeOverlay
        activeOverlayOpacity.value = draft.overlayOpacity
    }

    // Saves current parameters to active draft
    fun saveDraft(title: String? = null) {
        val current = _currentDraft.value ?: return
        viewModelScope.launch {
            val updatedTitle = title ?: current.title
            val updatedDraft = current.copy(
                title = updatedTitle,
                imageUrl = activeImageUrl.value,
                filterType = activeFilterType.value,
                filterIntensity = activeIntensity.value,
                brightness = activeBrightness.value,
                contrast = activeContrast.value,
                saturation = activeSaturation.value,
                vignette = activeVignette.value,
                grain = activeGrain.value,
                activeOverlay = activeOverlay.value,
                overlayOpacity = activeOverlayOpacity.value,
                timestamp = System.currentTimeMillis()
            )
            
            // Insert or Update to repository database
            val generatedId = repository.insertDraft(updatedDraft)
            
            // If it was a new project, preserve its generated database ID as a reference
            if (current.id == 0) {
                _currentDraft.value = updatedDraft.copy(id = generatedId.toInt())
            } else {
                _currentDraft.value = updatedDraft
            }
        }
    }

    // Deletes draft Project permanently
    fun deleteDraft(draft: Draft) {
        viewModelScope.launch {
            repository.deleteDraft(draft)
        }
    }

    // Deletes current active editor draft
    fun deleteActiveDraft() {
        val current = _currentDraft.value ?: return
        if (current.id != 0) {
            viewModelScope.launch {
                repository.deleteDraftById(current.id)
                exitEditor()
            }
        } else {
            exitEditor()
        }
    }

    // Close workspace, returns safely to Home Dashboard
    fun exitEditor() {
        _currentDraft.value = null
    }
}
