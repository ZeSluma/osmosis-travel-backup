# Sichtbare Zustands- und Übergangsprüfung — 2026-09-26

## Umfang und Entscheidung

Dieser Audit wurde nach realem S25-Feedback durchgeführt. Er prüft nicht nur statische Texte,
sondern ob eine Anzeige bei Lifecycle-, Service- und Stale-Übergängen ehrlich bleibt. Ergänzung
nach weiterem S25-Feedback: ein offener Integritäts-/Zuordnungsnachweis ist keine aktive Arbeit,
und ein abgeschlossener SSD-Worker muss die langlebige Projektion aktiv invalidieren. Ergebnis:
softwareseitig **PASS**. Reale Render-Timing- und Bluetooth-/Pocket-Varianten bleiben getrennte
Hardwarevalidierung.

| Sichtbarer Bereich | Aktiver Zustand | Fortschritt | Erfolgreiches Terminal | Fail-closed Terminal / stale Ersatz | Deterministische Evidenz |
|---|---|---|---|---|---|
| Kameralisten-/Backupzusammenfassung | `Backupstatus wird geladen` nur ohne durable Projektion | Kein erfundener Fortschritt | Verständliche vollständige/SSD-Statuszeile | Unvollständige Inventur: `Kamera-Sicherung wartet auf vollständige Dateiliste` | `BackupStatusCopyTest` |
| Automatische Sicherung | PLAN_LOOKUP, WRITER_WAIT | `WRITER_STARTED` plus gültiger Prozentfortschritt | `WRITER_COMPLETE`, 100 %, automatische Ausblendung nach 3 s | REVIEW, SOURCE_CHANGED, NO_WORK und unzulässige Entscheidungen: kein Balken | `AutomaticTransferUiStatePolicyTest` |
| Offener Integritäts-/Identitätsnachweis | `Synchronisation offen` mit der konkreten fehlenden Evidenz | Kein Balken, weil kein Worker läuft | Erst ein echter Dienst-Worker kann in einen aktiven Zustand wechseln | Kein „werden geprüft“/„werden erneut zugeordnet“ ohne gestarteten Worker; keine stille Promotion | `BackupStatusCopyTest` |
| Hintergrund/Rückkehr | Beobachter wird erneut registriert | Durable Projektion wird sofort erneut gelesen | Aktuelle Projektion wird dargestellt | Kein verlorenes Service-Publish kann einen alten Balken/Status konservieren | `BackupProjectionNotifierTest` |
| Externe SSD-Replikation | Hintergrundkopie schreibt nur nach gültiger SAF-Prüfung | Nicht anwendbar | Nach realem Kopierlauf wird der Observer einmal invalidiert und liest den neuen Ledger-Status | No-work-Verfügbarkeit veröffentlicht nicht und erzeugt keine UI-Refresh-Schleife | `ExternalReplicaRunPolicyTest`, `BackupProjectionNotifierTest`, source audit |
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
5. Offene `VERIFY_EXISTING`-/Revalidierungsplanposten wurden als fortlaufende Prüfung formuliert.
   Sie sind nun explizit offen, mit dem fehlenden Nachweis, und bleiben fail-closed.
6. Eine reale SSD-Kopie schrieb ihren finalen Ledger-Zustand ohne UI-Invalidierung. Der
   SSD-Koordinator publiziert nun einmal nach tatsächlichem Kopierlauf; ein no-work-Probe publiziert
   absichtlich nicht.

## Reproduzierbarer Checkpoint

`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon`:
**538 JVM-Tests, 0 failures, 0 errors, BUILD SUCCESSFUL**.

Debug-APK-SHA-256:
`ADB424D961F6566CE0F1C5790C24F3D76229D2EB7A8BC763020673A5B0E453F6`.

Keine dieser Tests behauptet einen realen Pocket-, S25- oder SSD-PASS. Der gebündelte
Hardwareablauf validiert anschließend nur noch seine physischen Aspekte.
