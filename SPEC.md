# Merge Run TD - Canonical MVP Spec

## Overview
Merge Run TD is a stage-based merge + lane defense game MVP for Android using Kotlin + Jetpack Compose.

## Core Run Flow
1. HOME -> player starts Stage 1.
2. RUN_SETUP initializes run state.
3. WAVE_START grants +1 free reroll and starts grace timer.
4. COMBAT processes spawn, movement, combat, and economy updates each tick.
5. INTERMISSION pauses between waves.
6. UPGRADE_CHOICE appears after wave 2 and wave 4.
7. RUN_END resolves win/lose.

## Mandatory Phases
- HOME
- RUN_SETUP
- WAVE_START
- COMBAT
- INTERMISSION
- UPGRADE_CHOICE
- RUN_END

## RunState (minimum contract)
- 4x4 board
- 3-slot shop
- active enemy list
- baseHP
- coins
- stage index
- wave index
- timers (wave, spawn, refill, grace, intermission, upgrade timeout)

## Lane/Timing Rules
- Lane length: 12 tiles.
- Five waves per stage.
- Wave durations: 12/13/13/14/15 sec.
- Wave grace: 0.8 sec.
- Intermission: 1.2 sec.
- Boss behavior: spawn boss around 2.0 sec after grace; escorts later.

## Economy Rules
- Shop slots: 3.
- Buy cost: 3 coins.
- Reroll cost: 2 coins.
- Sell refund: 1 coin.
- Empty slot refill interval: 2.5 sec.
- Free reroll +1 at each WAVE_START.

## Merge Rules
- Merge requirement: same role + same level.
- Result: level +1 unit.
- Role merge effects:
  - SHOOTER: extra shots
  - SPLASH: merge explosion
  - SLOW: slow field
  - WALL: knockback (+ optional stun through upgrade)
- WALL units have defensive HP (baseHp in unit definitions) in addition to attack stats.

## Upgrades Rules
- Offered after wave 2 and wave 4.
- Upgrade classes: INSTANT / RULE / TRANSFORM.
- Transform means a permanent modification to a role's behavior or stats (for example WALL gains stun, SLOW field bonus).
- Transform cap: max 1 per run.
- Timeout: 6 sec, then auto-pick Instant.

## Architecture Constraints
- Domain logic must be pure Kotlin and JVM unit-testable.
- Compose UI must be state-driven and intent-based.
- No combat/spawn/domain logic in composables.

## Source of Truth Files
- SPEC.md
- app/src/main/assets/stage_config.json
- app/src/main/assets/unit_defs.json
- app/src/main/assets/enemy_defs.json
- app/src/main/assets/upgrades.json
