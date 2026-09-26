# Sichtbare Zustands- und Übergangsprüfung — 2026-09-26

## Umfang und Entscheidung

Dieser Audit wurde nach realem S25-Feedback durchgeführt. Er prüft nicht nur statische Texte,
sondern ob eine Anzeige bei Lifecycle-, Service- und Stale-Übergängen ehrlich bleibt. Ergebnis:
softwareseitig **PASS**. Reale Render-Timing- und Bluetooth-/Pocket-Varianten bleiben getrennte
Hardwarevalidierung.

| Sichtbarer Bereich | Aktiver Zustand | Fortschritt | Erfolgreiches Terminal | Fail-closed Terminal / stale Ersatz | Deterministische Evidenz |
|---|---|---|---|---|---|
| Kameralisten-/Backupzusammenfassung | `Backupstatus wird geladen` nur ohne durable Projektion | Kein erfundener Fortschritt | Verständliche vollständige/SSD-Statuszeile | Unvollständige Inventur: `Kamera-Sicherung wartet auf vollständige Dateiliste` | `BackupStatusCopyTest` |
| Automatische Sicherung | PLAN_LOOKUP, WRITER_WAIT | `WRITER_STARTED` plus gültiger Prozentfortschritt | `WRITER_COMPLETE`, 100 %, automatische Ausblendung nach 3 s | REVIEW, SOURCE_CHANGED, NO_WORK und unzulässige Entscheidungen: kein Balken | `AutomaticTransferUiStatePolicyTest` |
| Hintergrund/Rückkehr | Beobachter wird erneut registriert | Durable Projektion wird sofort erneut gelesen | Aktuelle Projektion wird dargestellt | Kein verlorenes Service-Publish kann einen alten Balken/Status konservieren | `BackupProjectionNotifierTest` |
| Ersatz-/Stale-Callback | Neuer Serviceentscheid gewinnt | Nur `WRITER_STARTED` darf Fortschritt anzeigen | WRITER_COMPLETE verdrängt alten Fortschritt | SOURCE_CHANGED/REVIEW verdrängen verzögerten `transfer=…`-Wert | `AutomaticTransferUiStatePolicyTest` |
| Asset-/Produktzustände | Backend-Projektion | Nicht anwendbar | Verified/Redundancy/Safe-to-Clear nur aus Evidence | New/partial/ambiguous/incomplete nie hochgestuft | `ProductStateMatrixTest`, `BackupDisplayTest`, `ReplicaVerificationTest` |

## Reparierte Lücken

1. Terminal unvollständige Inventur wurde als scheinbar laufendes „wird geprüft“ dargestellt.
   Sie ist nun ein verständlicher, konservativer Wartezustand.
2. Ein Fortschrittsbereich konnte nach terminaler no-work/review/source-change-Entscheidung
   sichtbar bleiben. Terminale Entscheide löschen die Präsentation; Erfolg hat nur eine begrenzte
   Quittungsdauer.
3. Ein Hintergrundübergang konnte zwischen Service-Publish und UI-Neuregistrierung liegen.
   Neue Beobachter invalidieren nun sofort und lesen durable Wahrheit erneut.
4. Ein verspäteter Prozentfortschritt konnte einen späteren terminalen Entscheid übermalen.
   Terminale Entscheide dominieren; nur `WRITER_STARTED` kann einen Fortschrittswert anzeigen.

## Reproduzierbarer Checkpoint

`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon`:
**531 JVM-Tests, 0 failures, 0 errors, BUILD SUCCESSFUL**.

Debug-APK-SHA-256:
`601631F5FE8A2DD2E668FD0A262BBA0BA2370D3C797B982A25BBF034F2316F0D`.

Keine dieser Tests behauptet einen realen Pocket-, S25- oder SSD-PASS. Der gebündelte
Hardwareablauf validiert anschließend nur noch seine physischen Aspekte.
