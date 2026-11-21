package com.nordisapps.fmradio.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordisapps.fmradio.R
import com.nordisapps.fmradio.data.LanguageManager

@Composable
fun SettingsScreen(
    tuningStep: TuningStep,
    onTuningStepChange: (TuningStep) -> Unit,
    frequencyBand: FrequencyBand,
    onFrequencyBandChange: (FrequencyBand) -> Unit,
    isRdsEnabled: Boolean,
    onRdsToggle: (Boolean) -> Unit,
    isMonoMode: Boolean,
    onMonoModeToggle: (Boolean) -> Unit,
    isSoftMuteEnabled: Boolean,
    onSoftMuteToggle: (Boolean) -> Unit,
    onOpenDiagnostics: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var language by remember { mutableStateOf(LanguageManager.getLanguage(context)) }

    LazyColumn {
        item { SettingsSectionHeader(stringResource(R.string.settings_section_general)) }

        item {
            SettingsDropdownRow(
                title = stringResource(R.string.settings_language_title),
                selectedLabel = languageName(language),
                options = LanguageManager.SUPPORTED_LANGUAGES.toList(),
                optionLabel = { languageName(it) },
                onOptionSelected = { code ->
                    if (code != language) {
                        language = code
                        LanguageManager.saveLanguage(context, code)
                        context.findActivity()?.recreate()
                    }
                }
            )
        }

        item { SettingsSectionHeader(stringResource(R.string.settings_section_tuning)) }

        item {
            SettingsDropdownRow(
                title = stringResource(R.string.settings_tuning_step),
                selectedLabel = stringResource(tuningStep.labelRes),
                options = TuningStep.entries,
                optionLabel = { stringResource(it.labelRes) },
                onOptionSelected = onTuningStepChange
            )
        }

        item {
            SettingsDropdownRow(
                title = stringResource(R.string.settings_frequency_band),
                selectedLabel = stringResource(frequencyBand.labelRes),
                options = FrequencyBand.entries,
                optionLabel = { stringResource(it.labelRes) },
                onOptionSelected = onFrequencyBandChange
            )
        }

        if (frequencyBand == FrequencyBand.JAPAN_EXTENDED) {
            item {
                Text(
                    text = stringResource(R.string.settings_japan_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }
        }

        item { SettingsSectionHeader(stringResource(R.string.settings_section_audio)) }

        item {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_rds),
                subtitle = stringResource(R.string.settings_rds_subtitle),
                checked = isRdsEnabled,
                onCheckedChange = onRdsToggle
            )
        }

        item {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_mono),
                checked = isMonoMode,
                onCheckedChange = onMonoModeToggle
            )
        }

        item {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_softmute),
                subtitle = stringResource(R.string.settings_softmute_subtitle),
                checked = isSoftMuteEnabled,
                onCheckedChange = onSoftMuteToggle
            )
        }

        if (onOpenDiagnostics != null) {
            item { SettingsSectionHeader("Debug") }
            item {
                ListItem(
                    headlineContent = { Text("Diagnostics") },
                    supportingContent = { Text("FM service report for the developer") },
                    modifier = Modifier.clickable(onClick = onOpenDiagnostics)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(subtitle) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun <T> SettingsDropdownRow(
    title: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Row {
                TextButton(onClick = { isExpanded = true }) {
                    Text(selectedLabel)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionLabel(option)) },
                            onClick = {
                                onOptionSelected(option)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun languageName(code: String): String = when (code) {
    "ru" -> "Русский"
    else -> "English"
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

enum class TuningStep(@param:StringRes val labelRes: Int, val stepMhz: Double) {
    STEP_50_KHZ(R.string.step_50_khz, 0.05),
    STEP_100_KHZ(R.string.step_100_khz, 0.1),
    STEP_200_KHZ(R.string.step_200_khz, 0.2)
}

enum class FrequencyBand(@param:StringRes val labelRes: Int, val minMhz: Double, val maxMhz: Double) {
    STANDARD(R.string.band_standard, 87.5, 108.0),
    JAPAN(R.string.band_japan, 76.0, 90.0),
    JAPAN_EXTENDED(R.string.band_japan_extended, 76.0, 108.0)
}