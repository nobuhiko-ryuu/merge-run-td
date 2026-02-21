package com.example.mergeruntd.ui.run

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mergeruntd.domain.board.Board
import com.example.mergeruntd.domain.core.RunState
import com.example.mergeruntd.domain.lane.LaneState
import com.example.mergeruntd.domain.shop.ShopState

@Composable
fun runScreen(
    uiState: RunUiState,
    onBackToHome: () -> Unit,
) {
    when (uiState) {
        RunUiState.Idle -> Text("Preparing run...")
        is RunUiState.Error -> Text("Error: ${uiState.message}")
        is RunUiState.Running -> runContent(runState = uiState.runState, onBackToHome = onBackToHome)
    }
}

@Composable
private fun runContent(
    runState: RunState,
    onBackToHome: () -> Unit,
) {
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
        item { boardView(runState.board) }
        item { laneView(runState.lane) }
        item { shopView(runState.shop) }
    }
}

@Composable
private fun hudView(runState: RunState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("HUD", style = MaterialTheme.typography.titleMedium)
        Text("Coins: ${runState.coins}")
        Text("Base HP: ${runState.baseHp}")
        Text("Stage: ${runState.stageIndex + 1}")
        Text("Wave: ${runState.waveIndex + 1}")
        Text("Phase: ${runState.phase}")
    }
}

@Composable
private fun boardView(board: Board) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Board", style = MaterialTheme.typography.titleMedium)
        repeat(board.rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(board.cols) { col ->
                    val index = row * board.cols + col
                    val role = board.cells[index]?.role ?: ""
                    tileCell(text = role)
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
private fun shopView(shop: ShopState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Shop", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            shop.slots.forEach { slot ->
                tileCell(text = slot.unitId ?: "Empty", size = 72)
            }
        }
    }
}

@Composable
private fun tileCell(
    text: String,
    size: Int = 56,
) {
    Column(
        modifier =
            Modifier
                .size(size.dp)
                .border(1.dp, Color.Gray)
                .background(Color.White)
                .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
