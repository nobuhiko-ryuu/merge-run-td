# Merge Run TD — MVP Specification

## 1) Product Goal
Build a playable Android MVP for a **merge + lane-defense** game using **Kotlin + Jetpack Compose**.

### Core loop
1. Start a run (stage-based).
2. Buy units from shop and place on 4x4 board.
3. Merge same role+level units for stronger units and role-specific merge effects.
4. Survive 5 waves per stage while enemies traverse a 12-tile lane.
5. Pick upgrades after wave 2 and wave 4.
6. Win by clearing all waves with base HP > 0, lose if base HP reaches 0.

## 2) Technical Constraints
- Android app with single module `:app`.
- Kotlin + Compose UI.
- Domain logic must be pure Kotlin and JVM-testable.
- Composables must render state and dispatch intents only.
- No combat/spawn logic inside UI composables.

## 3) Run/State Model

### Game phases
- `HOME`
- `RUN_SETUP`
- `WAVE_START`
- `COMBAT`
- `INTERMISSION`
- `UPGRADE_CHOICE`
- `RUN_END`

### RunState (minimum)
- Board: 4x4 cells.
- Shop: 3 slots.
- Enemy list (active enemies in lane).
- Base HP.
- Coins.
- Stage and wave indexes.
- Timers:
  - wave timer
  - spawn timer/schedule timer
  - shop refill timer
  - grace timer (wave start)
  - intermission timer
  - upgrade timeout timer

## 4) Stage / Wave Timing Rules
- Each stage has 5 waves.
- Wave durations baseline: `12 / 13 / 13 / 14 / 15` seconds.
- Wave grace before regular combat: `0.8s`.
- Intermission between waves: `1.2s`.
- Stage progression target: Stage 1 completes in approximately 60–90s excluding upgrade pauses.

### Spawn rules
- Spawn totals must match `stage_config.json` for each wave.
- Enemy movement lane length: 12 tiles.
- Use enemy speed model from `enemy_defs.json`.
- Apply stage multipliers (`hpMul`, `spdMul`) to spawned enemies.

### Boss wave rules
- Boss wave is wave 5.
- Spawn boss around 2 seconds after wave grace.
- Spawn escorts after boss spawn, according to stage config.

## 5) Shop / Economy Rules
- Shop has 3 slots.
- Empty slot refills every `2.5s`.
- Buy level-1 unit cost: `3 coins`.
- Reroll all slots cost: `2 coins`.
- Free reroll count: `+1` at each `WAVE_START`.
- Sell unit returns `+1 coin` (50% of purchase price).
- If board full, purchase is blocked and state should expose a UI hint flag (e.g., “Sell something”).

## 6) Merge Rules
- Merge condition: same role + same level.
- Result: single unit with `level + 1`.

### Merge effects by role
- `SHOOTER`: extra shots.
- `SPLASH`: merge explosion.
- `SLOW`: merge slow field.
- `WALL`: knockback (optional stun enabled via upgrade).

## 7) Upgrade Rules
- Upgrade offer appears after wave 2 and wave 4.
- Upgrade composition: `Instant`, `Rule`, `Transform`.
- Max transform picks per run: **1**.
- Upgrade choice timeout: `6s`.
- On timeout, auto-pick an `Instant` upgrade.
- Upgrade parameters modify combat/economy systems (examples: `atkMul`, `aspdMul`, etc.).

## 8) Data Files (Single Source of Truth)
- `SPEC.md`
- `app/src/main/assets/stage_config.json`
- `app/src/main/assets/unit_defs.json`
- `app/src/main/assets/enemy_defs.json`
- `app/src/main/assets/upgrades.json`

## 9) Testing Requirements
- Domain state machine transitions must be unit tested.
- Win/lose condition skeleton tests required early.
- Spawn totals per wave must be tested against stage config.
- Economy tests: buy/reroll/free-reroll/sell with coin updates.
- Merge + upgrade tests:
  - merge level-up
  - role effect trigger hooks
  - upgrade apply
  - transform cap

## 10) MVP UI Screens
- Home: start stage 1.
- Game: HUD + lane + board + shop + reroll + sell.
- Result: win/lose + retry.

### MVP input patterns
- Tap shop item then board cell to place.
- Merge by drag-onto or tap-select then tap target.
- Sell by drag-to-sell-zone or select + Sell button.

## 11) Non-goals for MVP
- Ads/IAP SDKs.
- Advanced VFX polish.
- Multiplayer or online features.
