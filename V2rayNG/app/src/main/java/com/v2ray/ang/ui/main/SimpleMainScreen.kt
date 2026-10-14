package com.v2ray.ang.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.handler.MmkvManager

/**
 * Simple home screen: one big connect button + "paste key".
 * The gear opens the original full screen (servers, subscriptions, settings).
 */
@Composable
fun SimpleMainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    if (showAdvanced) {
        BackHandler { showAdvanced = false }
        MainScreen(
            mainViewModel = mainViewModel,
            onAction = onAction,
            onNavigate = onNavigate,
        )
        return
    }

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val isRunning = uiState.isRunning
    val statusText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val serverName = if (selectedGuid.isNullOrEmpty()) {
        null
    } else {
        MmkvManager.decodeServerConfig(selectedGuid)?.remarks?.takeIf { it.isNotBlank() }
    }
    val hasServer = !selectedGuid.isNullOrEmpty()

    val accent = MaterialTheme.colorScheme.primary
    val buttonColor by animateColorAsState(
        targetValue = if (isRunning) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "connectButtonColor"
    )
    val iconColor = if (isRunning) MaterialTheme.colorScheme.onPrimary else accent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showAdvanced = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_24dp),
                    contentDescription = "Расширенные настройки",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(176.dp)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable { onAction(MainAction.ToggleService) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp
                ),
                contentDescription = if (isRunning) "Отключить" else "Подключить",
                tint = iconColor,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (isRunning) "Подключено" else "Не подключено",
            style = MaterialTheme.typography.headlineSmall,
            color = if (isRunning) accent else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                !hasServer -> "Сначала вставь ключ"
                serverName != null -> serverName
                else -> statusText
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (isRunning && hasServer) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (hasServer) {
            OutlinedButton(
                onClick = { onAction(MainAction.ImportClipboard) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                border = BorderStroke(1.dp, accent)
            ) {
                Text(text = "Вставить другой ключ", color = accent)
            }
        } else {
            Button(
                onClick = { onAction(MainAction.ImportClipboard) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Вставить ключ")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
