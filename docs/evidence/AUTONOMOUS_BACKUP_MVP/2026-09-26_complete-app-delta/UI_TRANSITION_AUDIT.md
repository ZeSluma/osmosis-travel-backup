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
| Kurzzeitig beschäftigte Kamera | Vor dem ersten Byte: exakt betroffene Kachel und Karte „Kamera antwortet noch“ | Indeterminierter Balken ohne erfundenen Prozentwert; nach Antwort gemessener Telefon-Prozentwert | Erfolg wechselt in normalen Transfer und danach langlebigen Beleg | Nach höchstens drei 404/500-Wiederholungen fail-closed Review und langlebiger Reread; 403 wird nicht wiederholt | `CameraTransferSourceTest`, `LiveTransferFileProjectionPolicyTest`, source-order audit |
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
7. Der strenge automatische Kamera-Pfad simulierte keine vorübergehende 404/500-Antwort vor dem
   ersten Byte. Er behandelt sie jetzt als begrenzte, sichtbare Wiederholung vor jeder
   Dateiallokation; andere Antworten und erschöpfte Wiederholungen bleiben Review.

## Reproduzierbarer Checkpoint

`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon`:
**541 JVM-Tests, 0 failures, 0 errors, BUILD SUCCESSFUL**.

Debug-APK-SHA-256:
`D788F45892CE67E3B7546E0EFA22D42852AC95F058D5DB3105507DD38310CDEE`.

## Nachtrag: idempotentes Raster-Rendering

Die nächste Zielgeräterückmeldung zeigte hochfrequentes Springen des gesamten Rasters einschließlich
Filterleiste. Ursache war kein neuer Produktzustand, sondern `notifyItemRangeChanged(0, itemCount)`
bei jeder Service-Publikation. `BackupProjectionDiffPolicy` vergleicht nun langlebige und flüchtige
Projektion nach exakter Dateischlüsselbindung: identisches Publish ist ein No-op; Fortschritt und
terminales Overlay aktualisieren nur die betroffene Kachel. `BackupProjectionDiffPolicyTest` deckt
genau diese drei Fälle ab. Der neue Vollcheckpoint hat **544 JVM-Tests ohne Fehler** und APK SHA-256
`188D877671CD8D4331E129DFE29A8469C95647D13CBBC78289A24E8A11E5E388`.

Keine dieser Tests behauptet einen realen Pocket-, S25- oder SSD-PASS. Der gebündelte
Hardwareablauf validiert anschließend nur noch seine physischen Aspekte.
