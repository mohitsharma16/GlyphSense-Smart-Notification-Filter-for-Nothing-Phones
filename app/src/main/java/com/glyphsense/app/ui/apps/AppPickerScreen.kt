package com.glyphsense.app.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glyphsense.app.R
import com.glyphsense.app.domain.PriorityLevel
import com.glyphsense.app.ui.components.AppIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    onBack: () -> Unit,
    viewModel: AppPickerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apps_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(R.string.apps_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                CountsRow(
                    important = state.importantCount,
                    normal = state.normalCount,
                    silent = state.silentCount
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text(stringResource(R.string.search_apps)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            HorizontalDivider()

            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.rows.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.query.isBlank())
                                stringResource(R.string.empty_no_apps)
                            else
                                stringResource(R.string.empty_no_results, state.query),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.rows, key = { it.app.packageName }) { row ->
                            AppRowItem(
                                row = row,
                                onPriorityChange = { level ->
                                    viewModel.setPriority(row.app.packageName, level)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountsRow(important: Int, normal: Int, silent: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CountChip(important, stringResource(R.string.priority_important))
        CountChip(normal, stringResource(R.string.priority_normal))
        CountChip(silent, stringResource(R.string.priority_silent))
    }
}

@Composable
private fun CountChip(count: Int, label: String) {
    Column {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppRowItem(
    row: AppRow,
    onPriorityChange: (PriorityLevel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIcon(drawable = row.app.icon, modifier = Modifier.size(40.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = row.app.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                text = row.app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PriorityChip(
            label = stringResource(R.string.priority_important),
            selected = row.priority == PriorityLevel.IMPORTANT,
            onClick = { onPriorityChange(PriorityLevel.IMPORTANT) }
        )
        PriorityChip(
            label = stringResource(R.string.priority_normal),
            selected = row.priority == PriorityLevel.NORMAL,
            onClick = { onPriorityChange(PriorityLevel.NORMAL) }
        )
        PriorityChip(
            label = stringResource(R.string.priority_silent),
            selected = row.priority == PriorityLevel.SILENT,
            onClick = { onPriorityChange(PriorityLevel.SILENT) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}
