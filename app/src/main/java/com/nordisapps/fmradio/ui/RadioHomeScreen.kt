package com.nordisapps.fmradio.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.nordisapps.fmradio.RadioViewModel

@Composable
fun RadioHomeScreen(viewModel: RadioViewModel, snackbarHostState: SnackbarHostState) {
    val uiState = viewModel.uiState

    val text = uiState.readinessMessage?.let { stringResource(it) }
    LaunchedEffect(text) {
        text?.let { snackbarHostState.showSnackbar(it); viewModel.clearReadinessMessage() }
    }

    RadioPlayerScreen(
        currentFrequency = uiState.currentFrequency,
        stationName = uiState.stationName,
        radioText = uiState.radioText,
        isPlaying = uiState.isPlaying,
        isRecording = uiState.isRecording,
        isRecordingPaused = uiState.isRecordingPaused,
        recordingTime = uiState.recordingTime,
        scannedStations = uiState.scannedStations,
        savedStations = uiState.savedStations,
        favoriteStations = uiState.favoriteStations,
        showScannedStations = uiState.showScannedStations,
        tuningStep = uiState.tuningStep,
        frequencyBand = uiState.frequencyBand,
        onSeekDownClick = viewModel::seekDown,
        onSeekUpClick = viewModel::seekUp,
        onScaleFrequencyChange = viewModel::onScaleFrequencyChange,
        onConfirmScannedStations = viewModel::confirmScannedStations,
        onSavedStationSelected = viewModel::tuneToStation,
        onFavoriteToggle = viewModel::toggleFavoriteAndPersist
    )
}