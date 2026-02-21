package com.example.mergeruntd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    HELP,
}

@Composable
private fun mergeRunTdApp() {
    val context = LocalContext.current
    val runViewModel: RunViewModel = viewModel { RunViewModel(context.applicationContext) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val uiState by runViewModel.uiState.collectAsStateWithLifecycle()

    when (currentScreen) {
        Screen.HOME ->
            homeScreen(
                onPlayClick = {
                    runViewModel.startRun()
                    currentScreen = Screen.RUN
                },
                onHowToPlayClick = {
                    currentScreen = Screen.HELP
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
                onBuyFromShop = runViewModel::buyFromShop,
                onRerollShop = runViewModel::rerollShop,
                onBoardCellTapped = runViewModel::onBoardCellTapped,
                onSellSelected = runViewModel::sellSelected,
                onSelectUpgrade = runViewModel::selectUpgrade,
                onRetry = runViewModel::retryRun,
                onNextStage = runViewModel::nextStageRun,
            )
        }

        Screen.HELP -> howToPlayScreen(onBack = { currentScreen = Screen.HOME })
    }
}

@Composable
private fun homeScreen(
    onPlayClick: () -> Unit,
    onHowToPlayClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Merge Run TD", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onPlayClick) {
            Text(text = "Play")
        }
        Button(onClick = onHowToPlayClick) {
            Text(text = "How to play")
        }
    }
}

@Composable
private fun howToPlayScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "How to Play", style = MaterialTheme.typography.headlineSmall)
        Text("• Buy from Shop and place units on the board")
        Text("• Merge identical units to power up")
        Text("• Use Reroll to refresh shop slots")
        Text("• Sell selected units to recover coins")
        Text("• Upgrades appear after wave 2/4 and auto-pick on timeout")
        Text("• Lose when HP reaches 0, win by surviving 5 waves")
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
