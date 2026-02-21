package com.example.mergeruntd.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.core.RunEnd
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.shop.ShopState
import com.example.mergeruntd.ui.components.AppCard
import com.example.mergeruntd.ui.components.AppTile
import com.example.mergeruntd.ui.components.MessageBar
import com.example.mergeruntd.ui.components.PrimaryButton
import com.example.mergeruntd.ui.components.SecondaryButton
import kotlin.math.ceil

@Composable
fun runScreen(
    uiState: RunUiState,
    onBackToHome: () -> Unit,
    onBuyFromShop: (Int) -> Unit,
    onRerollShop: () -> Unit,
    onBoardCellTapped: (Int) -> Unit,
    onSellSelected: () -> Unit,
    onSelectUpgrade: (Int) -> Unit,
    onRetry: () -> Unit,
    onNextStage: () -> Unit,
) {
    when (uiState) {
        RunUiState.Idle -> Text("Preparing run...")
        is RunUiState.Error -> MessageBar(message = "Error: ${uiState.message}", isError = true)
        is RunUiState.Running ->
            runContent(
                uiState = uiState,
                onBackToHome = onBackToHome,
                onBuyFromShop = onBuyFromShop,
                onRerollShop = onRerollShop,
                onBoardCellTapped = onBoardCellTapped,
                onSellSelected = onSellSelected,
                onSelectUpgrade = onSelectUpgrade,
                onRetry = onRetry,
                onNextStage = onNextStage,
            )
    }
}

@Composable
private fun runContent(
    uiState: RunUiState.Running,
    onBackToHome: () -> Unit,
    onBuyFromShop: (Int) -> Unit,
    onRerollShop: () -> Unit,
    onBoardCellTapped: (Int) -> Unit,
    onSellSelected: () -> Unit,
    onSelectUpgrade: (Int) -> Unit,
    onRetry: () -> Unit,
    onNextStage: () -> Unit,
) {
    val runState = uiState.runState
    val offerActive = runState.offeredUpgrade != null
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SecondaryButton(onClick = onBackToHome, text = "Back")
        }
        item { hudView(runState) }
        item {
            MessageBar(message = uiState.message, isError = true)
        }
        item {
            boardView(
                board = runState.board,
                selectedCellIndex = uiState.selectedCellIndex,
                onCellTapped = onBoardCellTapped,
                enabled = !offerActive,
            )
        }
        item {
            if (uiState.selectedCellIndex != null && runState.board.cells.getOrNull(uiState.selectedCellIndex) != null) {
                SecondaryButton(onClick = onSellSelected, enabled = !offerActive, text = "Sell (+1)")
            }
        }
        item { laneView(runState.lane) }
        item {
            shopView(
                shop = runState.shop,
                onBuyFromShop = onBuyFromShop,
                onRerollShop = onRerollShop,
                enabled = !offerActive,
            )
        }
    }

    runState.offeredUpgrade?.let { offer ->
        val remainMs = (offer.deadlineTimeMs - runState.timeMs).coerceAtLeast(0)
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Choose Upgrade") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Wave ${offer.waveIndex} clear reward")
                    Text("Auto pick in ${ceil(remainMs / 1000.0).toInt()} sec")
                    offer.options.forEachIndexed { index, option ->
                        SecondaryButton(onClick = { onSelectUpgrade(index) }, text = "${option.name} (${option.type})")
                    }
                }
            },
            confirmButton = {},
        )
    }

    runState.end?.let { end ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("RUN_END") },
            text = { Text(if (end == RunEnd.Victory) "Victory" else "Defeat") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton(onClick = onRetry, text = "Retry")
                    SecondaryButton(onClick = onNextStage, text = "Next Stage")
                    SecondaryButton(onClick = onBackToHome, text = "Home")
                }
            },
        )
    }
}

@Composable
private fun hudView(runState: RunState) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("HUD", style = MaterialTheme.typography.titleMedium)
            Text("Coins: ${runState.coins}")
            Text("Free reroll: ${runState.freeRerollLeft}")
            Text("Base HP: ${runState.baseHp}")
            Text("Stage: ${runState.stageIndex + 1}")
            Text("Wave: ${runState.waveIndex + 1}")
            Text("Phase: ${runState.phase}")
            Text("ATK x${"%.2f".format(runState.atkMul)} / ASPD x${"%.2f".format(runState.aspdMul)}")
        }
    }
}

@Composable
private fun boardView(
    board: Board,
    selectedCellIndex: Int?,
    onCellTapped: (Int) -> Unit,
    enabled: Boolean,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Board", style = MaterialTheme.typography.titleMedium)
            repeat(board.rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(board.cols) { col ->
                        val index = row * board.cols + col
                        val unit = board.cells[index]
                        val text = if (unit == null) "" else "${unit.role}\nLv${unit.level}"
                        AppTile(
                            text = text,
                            selected = index == selectedCellIndex,
                            onClick = if (enabled) ({ onCellTapped(index) }) else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun laneView(lane: LaneState) {
    val enemiesByTile = lane.enemies.groupBy { it.tile }.mapValues { it.value.size }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Lane", style = MaterialTheme.typography.titleMedium)
            Text("Enemies: ${lane.enemies.size}")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(lane.length) { tile ->
                    val count = enemiesByTile[tile] ?: 0
                    AppTile(text = if (count == 0) "" else count.toString(), size = 24.dp)
                }
            }
        }
    }
}

@Composable
private fun shopView(
    shop: ShopState,
    onBuyFromShop: (Int) -> Unit,
    onRerollShop: () -> Unit,
    enabled: Boolean,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Shop", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                shop.slots.forEachIndexed { index, slot ->
                    AppTile(
                        text = slot.unitId ?: "Empty",
                        size = 72.dp,
                        onClick = if (enabled) ({ onBuyFromShop(index) }) else null,
                    )
                }
            }
            PrimaryButton(onClick = onRerollShop, enabled = enabled, text = "Reroll")
        }
    }
}
