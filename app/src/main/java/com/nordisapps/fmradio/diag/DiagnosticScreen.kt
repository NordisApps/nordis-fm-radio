package com.nordisapps.fmradio.diag

import android.content.ClipData
import android.content.ClipboardManager
import android.os.IBinder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun DiagnosticScreen(binderProvider: () -> IBinder?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun run(includeNew: Boolean) {
        scope.launch {
            busy = true
            report = DiagnosticReport.build(context, binderProvider(), includeNew)
            busy = false
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "FM diagnostics. Turn off radio, connect headphones and click «Old + New». " +
                    "In report only data about phone and FM-service.",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { run(false) }, enabled = !busy) { Text("Old") }
            Button(onClick = { run(true) }, enabled = !busy) { Text("Old + New") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = report.isNotEmpty(),
                onClick = {
                    context.getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("fm-report", report))
                }
            ) { Text("Copy") }
            OutlinedButton(
                enabled = report.isNotEmpty(),
                onClick = { DiagnosticReport.share(context, report) }
            ) { Text("Share") }
        }
        SelectionContainer(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            Text(report, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}