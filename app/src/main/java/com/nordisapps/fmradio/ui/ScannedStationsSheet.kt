package com.nordisapps.fmradio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nordisapps.fmradio.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannedStationsSheet(
    stations: List<Double>,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onConfirm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.stations_found, stations.size),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (stations.isEmpty()) {
                Text(
                    text = stringResource(R.string.stations_not_found),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(bottom = 12.dp)) {
                    items(stations) { freq ->
                        Text(
                            text = stringResource(R.string.freq_mhz, freq),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        HorizontalDivider()
                    }
                }
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.stations_sheet_action_confirm))
            }
        }
    }
}