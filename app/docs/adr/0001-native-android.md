
# ADR-0001: Native Android and offline-first architecture

Status: Accepted

## Context
Custodes Command will initially be used on Android
devices during tabletop games.

Reliable offline access and fast rule consultation
are more important than cross-platform support.

## Decision
Use Kotlin, Jetpack Compose and local persistence.

Organize the app with UI and data layers, keeping
complex Warhammer rules logic independently testable.

## Consequences
Positive:
- Native Android user experience
- Offline functionality
- Straightforward integration with Android APIs
- Business logic can be tested without an emulator

Trade-offs:
- Android-specific user interface
- A separate UI may be required for other platforms
- Local data migration must be managed carefully

## Future considerations
Keep calculation and validation logic independent
from Android frameworks where practical.
