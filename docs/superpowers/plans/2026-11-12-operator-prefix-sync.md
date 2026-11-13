# Operator Prefix Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline; user requested immediate implementation).

**Goal:** Sync `operator_prefixes` from Supabase into Room and prefer Room for offline operator detection, with `OperatorPrefixTable.Default` as fallback.

**Architecture:** Separate `OperatorPrefixRepository` with once-per-process refresh (catalog twin). Top-up and Recharge call `SyncOperatorPrefixesUseCase`. `RechargeViewModel` observes Room prefixes via Flow and passes a resolved `OperatorPrefixTable` into pure `DetectOperatorUseCase`.

**Tech Stack:** Supabase Postgrest, Room, Koin, Coroutines/Flow, JUnit

**Global Constraints:** Keep `OperatorPrefixTable`; no feature→feature deps; convention plugins only; do not commit unless user asks.

### Task 1: Supabase client read access
### Task 2: Room entity/DAO + DB version
### Task 3: Remote DTO + fetch
### Task 4: Repository + use cases + detection resolve helper
### Task 5: ViewModels + DI
### Task 6: Unit/android tests + docs

Spec: `docs/superpowers/specs/2026-11-12-operator-prefix-sync-design.md`
