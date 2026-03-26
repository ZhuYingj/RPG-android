package com.mobile_client.viewModels

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.TutorialService
import com.mobile_client.utils.TutorialSteps
import kotlinx.coroutines.launch

class TutorialViewModel : ViewModel() {
    private val service = TutorialService.instance

    var isVisible = mutableStateOf(false)
        private set
    var currentStep = mutableIntStateOf(0)
        private set
    var savedProgress = mutableIntStateOf(0)
        private set

    val totalSteps = TutorialSteps.steps.size

    val isLastStep: Boolean
        get() = currentStep.intValue >= totalSteps - 1

    val isFirstStep: Boolean
        get() = currentStep.intValue <= 0

    val progressFraction: Float
        get() = (currentStep.intValue + 1).toFloat() / totalSteps.toFloat()

    fun show() {
        isVisible.value = true
    }

    fun close() {
        isVisible.value = false
    }

    fun nextStep() {
        if (!isLastStep) {
            currentStep.intValue++
            saveProgress()
        } else {
            // Finish tutorial
            currentStep.intValue = totalSteps
            saveProgress()
            close()
        }
    }

    fun previousStep() {
        if (!isFirstStep) {
            currentStep.intValue--
        }
    }

    fun continueFromSaved() {
        currentStep.intValue = savedProgress.intValue
        if (currentStep.intValue >= totalSteps) {
            currentStep.intValue = totalSteps - 1
        }
        show()
    }

    fun restart() {
        currentStep.intValue = 0
        viewModelScope.launch {
            service.resetProgress()
        }
        show()
    }

    fun loadProgress() {
        viewModelScope.launch {
            val progress = service.getProgress()
            savedProgress.intValue = progress
            currentStep.intValue = progress
        }
    }

    fun showIfFirstTime() {
        viewModelScope.launch {
            val progress = service.getProgress()
            savedProgress.intValue = progress
            currentStep.intValue = progress
            if (progress == 0) {
                show()
            }
        }
    }

    private fun saveProgress() {
        savedProgress.intValue = currentStep.intValue
        viewModelScope.launch {
            service.updateProgress(currentStep.intValue)
        }
    }
}
