
# ADR-0002: Navigation and UI State

Status: Accepted

## Context
Custodes Command requires multiple top-level
sections and contextual detail screens.

Users must be able to return to their previous
screen while consulting rules during games.

## Decision
Use Jetpack Navigation 3 and Material 3.

Keep navigation state in the application shell.

Use simple string keys for the initial prototype.

Store small UI state with rememberSaveable.

## Consequences
Positive:
- Compose-first navigation
- Clear screen responsibilities
- Android back navigation
- State restoration for simple interactions
- Centralized navigation configuration

Trade-offs:
- Initial implementation uses one active stack
- Tab switching discards the previous tab's stack
- String keys are not fully type-safe

## Future improvements
- Introduce type-safe navigation keys
- Maintain separate back stacks for each tab
- Add ViewModels for feature-level state
- Introduce permanent local storage
