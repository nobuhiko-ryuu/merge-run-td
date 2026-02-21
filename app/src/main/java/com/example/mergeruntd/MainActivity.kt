package com.example.mergeruntd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mergeruntd.ui.run.runScreen
import com.example.mergeruntd.ui.theme.mergeruntdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            mergeruntdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    mergeRunTdApp()
                }
            }
        }
    }
}

private enum class Screen {
    HOME,
    RUN,
}

@Composable
private fun mergeRunTdApp(runViewModel: RunViewModel = viewModel { RunViewModel(applicationContext) }) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val uiState by runViewModel.uiState.collectAsStateWithLifecycle()

    when (currentScreen) {
        Screen.HOME ->
            homeScreen(
                onPlayClick = {
                    runViewModel.startRun()
                    currentScreen = Screen.RUN
                },
            )

        Screen.RUN -> {
            DisposableEffect(Unit) {
                runViewModel.onRunScreenActive(true)
                onDispose {
                    runViewModel.onRunScreenActive(false)
                }
            }
            runScreen(
                uiState = uiState,
                onBackToHome = {
                    runViewModel.onRunScreenActive(false)
                    currentScreen = Screen.HOME
                },
            )
        }
    }
}

@Composable
private fun homeScreen(onPlayClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Merge Run TD", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onPlayClick) {
            Text(text = "Play")
        }
    }
}
