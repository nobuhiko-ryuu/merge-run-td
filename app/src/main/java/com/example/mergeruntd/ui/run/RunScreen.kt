package com.example.mergeruntd.ui.run

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.core.RunEnd
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.shop.ShopState
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
        is RunUiState.Error -> Text("Error: ${uiState.message}")
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
            Button(onClick = onBackToHome) {
                Text("Back")
            }
        }
        item { hudView(runState) }
        item {
            if (uiState.message != null) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }
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
                Button(onClick = onSellSelected, enabled = !offerActive) {
                    Text("Sell (+1)")
                }
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
                        Button(onClick = { onSelectUpgrade(index) }) {
                            Text("${option.name} (${option.type})")
                        }
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
                    Button(onClick = onRetry) { Text("Retry") }
                    Button(onClick = onNextStage) { Text("Next Stage") }
                    Button(onClick = onBackToHome) { Text("Home") }
                }
            },
        )
    }
}

@Composable
private fun hudView(runState: RunState) {
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

@Composable
private fun boardView(
    board: Board,
    selectedCellIndex: Int?,
    onCellTapped: (Int) -> Unit,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Board", style = MaterialTheme.typography.titleMedium)
        repeat(board.rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(board.cols) { col ->
                    val index = row * board.cols + col
                    val unit = board.cells[index]
                    val text = if (unit == null) "" else "${unit.role}\nLv${unit.level}"
                    tileCell(
                        text = text,
                        isSelected = index == selectedCellIndex,
                        onTap = if (enabled) ({ onCellTapped(index) }) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun laneView(lane: LaneState) {
    val enemiesByTile = lane.enemies.groupBy { it.tile }.mapValues { it.value.size }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Lane", style = MaterialTheme.typography.titleMedium)
        Text("Enemies: ${lane.enemies.size}")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(lane.length) { tile ->
                val count = enemiesByTile[tile] ?: 0
                tileCell(text = if (count == 0) "" else count.toString(), size = 24)
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Shop", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            shop.slots.forEachIndexed { index, slot ->
                tileCell(
                    text = slot.unitId ?: "Empty",
                    size = 72,
                    onTap = if (enabled) ({ onBuyFromShop(index) }) else null,
                )
            }
        }
        Button(onClick = onRerollShop, enabled = enabled) {
            Text("Reroll")
        }
    }
}

@Composable
private fun tileCell(
    text: String,
    size: Int = 56,
    isSelected: Boolean = false,
    onTap: (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier
                .size(size.dp)
                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White)
                .let { modifier -> if (onTap != null) modifier.clickable { onTap() } else modifier }
                .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
