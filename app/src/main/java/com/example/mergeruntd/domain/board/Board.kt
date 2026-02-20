package com.example.mergeruntd.domain.board

import com.example.mergeruntd.domain.core.GameConstants

data class UnitInstance(
  val id: String,
  val role: String,
  val cooldownMs: Long = 0L,
)

data class Board(
  val rows: Int = GameConstants.BOARD_ROWS,
  val cols: Int = GameConstants.BOARD_COLS,
  val cells: List<UnitInstance?> =
    List(GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) { null },
) {
  fun allUnits(): List<UnitInstance> = cells.filterNotNull()

  fun withUpdatedUnits(units: List<UnitInstance>): Board {
    val updatedCells = cells.toMutableList()
    var unitIdx = 0
    for (index in updatedCells.indices) {
      if (updatedCells[index] != null) {
        if (unitIdx >= units.size) break
        updatedCells[index] = units[unitIdx]
        unitIdx++
      }
    }
    return copy(cells = updatedCells)
  }
}
