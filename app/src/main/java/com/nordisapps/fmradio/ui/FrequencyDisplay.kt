package com.nordisapps.fmradio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nordisapps.fmradio.R
import kotlin.math.roundToInt

@Composable
fun FrequencyDisplay(
    currentFrequency: String,
    frequencyBand: FrequencyBand,
    tuningStep: TuningStep,
    onFrequencyConfirmed: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = stringResource(
            R.string.freq_mhz,
            currentFrequency.ifBlank { frequencyBand.minMhz.toString() }
        ),
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(bottom = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showDialog = true }
    )

    if (showDialog) {
        FrequencyInputDialog(
            initialValue = currentFrequency,
            frequencyBand = frequencyBand,
            tuningStep = tuningStep,
            onDismiss = { showDialog = false },
            onConfirm = { newFreq ->
                onFrequencyConfirmed(newFreq)
                showDialog = false
            }
        )
    }
}

@Composable
private fun FrequencyInputDialog(
    initialValue: String,
    frequencyBand: FrequencyBand,
    tuningStep: TuningStep,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    val value = text.toDoubleOrNull()
    val range = frequencyBand.minMhz..frequencyBand.maxMhz
    val isValid = value != null && value in range
    val stepsPerMhz = (1f / tuningStep.stepMhz).roundToInt()
    val maxDecimalDigits = if (tuningStep.stepMhz < 0.1f) 2 else 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.freq_dialog_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { newValue ->
                    val filtered = newValue.filterIndexed { index, c ->
                        c.isDigit() || (c == '.' && !newValue.take(index).contains('.'))
                    }
                    val parts = filtered.split(".")
                    text = when (parts.size) {
                        1 -> parts[0].take(3)
                        2 -> parts[0].take(3) + "." + parts[1].take(maxDecimalDigits)
                        else -> filtered
                    }
                },
                label = {
                    Text(
                        stringResource(
                            R.string.freq_dialog_hint,
                            frequencyBand.minMhz, frequencyBand.maxMhz, tuningStep.stepMhz
                        )
                    )
                },
                isError = text.isNotEmpty() && !isValid,
                supportingText = {
                    if (text.isNotEmpty() && !isValid) {
                        Text(
                            stringResource(
                                R.string.freq_dialog_error,
                                frequencyBand.minMhz,
                                frequencyBand.maxMhz
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    value?.let {
                        val rounded = (it * stepsPerMhz).roundToInt() / stepsPerMhz.toDouble()
                        onConfirm(rounded)
                    }
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.freq_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}