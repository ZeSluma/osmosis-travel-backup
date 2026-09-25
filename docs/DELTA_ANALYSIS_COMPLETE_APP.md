# Delta-Analyse — vollständige Osmosis Travel Backup App

Stand: 2026-09-25. Quelle: aktuelle Code-/Testnachweise, `PROJECT_STATE.yaml`,
`PRODUCT_COMPLETION_PLAN.md`, Designanforderungen und die heutige S25/Pocket-Beobachtung.

## Maßstab

„Vollständig funktionsfähig“ bedeutet hier: jede autorisierte nicht-destruktive Produktfunktion ist
implementiert, reproduzierbar getestet und auf der vorgesehenen Hardware validiert. Eine Funktion
mit nur Fake-/JVM-Nachweis ist nicht hardware-validiert. Kamera-Löschung, Release und
Produktionszugänge bleiben eigene, explizit freizugebende Grenzen.

## Delta A — aktueller Kernfehler

| ID | Lücke | Nächste Umsetzung | Nachweis für Abschluss |
|---|---|---|---|
| PC20 | Nach automatischer Verbindung und sieben sichtbaren Videos blieb `Automatisch: Kameraliste wird noch geprüft`. Sieben sichtbare Elemente sind keine Vollständigkeitsgarantie: der Datalink kann weitere Seiten signalisieren. | **Diagnosefortschritt:** fehlende durable Projektion wird nun separat als `Backupstatus wird geladen` angezeigt und kann nicht mehr als unvollständige Kameraliste fehlinterpretiert werden. Terminales Datalink-Inventar weiterhin bis `LedgerCoordinator`/Planprojektion auf Zielgerät prüfen. | `BackupStatusCopyTest` und `CameraDatalinkCoordinatorTest` PASS; Debug-Build und eine Wiederholung von HD01 ohne Rescan. |

## Delta B — vollständige nicht-destruktive Produktfunktionen

| Bereich | Offene Arbeit | Test-/Abschlusskriterium |
|---|---|---|
| R-037 Capture-Day | **Software-Teilnachweis vorhanden:** lokale Zeitauflösung, Pfadreservierung, Gruppen-/Pfadstabilität über Reconcile und Neustart, Sidecar-Mitglieder sowie SAF-Pfadvalidierung sind durch JVM- und Emulator-Fixtures abgedeckt. Offen bleiben die CD01–CD08-Integration als Matrix und reale Pocket-Zeit-/Zeitzonen-/MIME-Fakten. | Die vorhandenen deterministischen Fixtures in einem aktuellen Emulatorlauf ausführen und eine gebündelte S25/Pocket-Beobachtung für Zeitquelle, Mitternacht, Sidecars und Zielordner erfassen. |
| R-042 Liveness | **Software-Teilnachweis vorhanden:** epoch-gefence-te Recovery-State-Machine und Wiederanlauf-Fault-Tests. Offen sind KA01–KA07: Idle-/Aktivtransfer-Verhalten, Screen-off, kontrollierter Verlust, kausale Einordnung und modellbezogene Keepalive-/Power-Settings. | Service-/Fault-Matrix vervollständigen plus ein realer, zeitlich erfasster S25/Pocket-Liveness-Test. Keine unbelegte Keepalive- oder Power-Setting-Behauptung. |
| R-040 GPS-Sync | Bestehender opt-in GPS-Foreground-Service und UI-Sperre verhindern gleichzeitiges Medien-Offload im selben Prozess. Offen: dauerhafte, prozessübergreifende Modus-Arbitrierung, expliziter Berechtigungs-/Ablehnungsnachweis und Hardwarevalidierung; Backup darf bei GPS aus/abgelehnt nicht beeinträchtigt sein. | Unit/Instrumentation für Berechtigungs-/Arbiterzustände; echte opt-in-Hardwarevalidierung nur mit separater Nutzerfreigabe. |
| R-041 Diagnostik | **Teilweise umgesetzt am 2026-09-25:** `DiagnosticEventStore` speichert Session-, Transfer-, Storage- und User-Aktion-Zustände app-privat, auf sieben Tage/10 MiB begrenzt, als ausschließlich erlaubnislistenbasierte Codes und kann nur diese Repräsentation explizit exportieren. Der separate Verbose-Pfad ist ebenfalls app-privat, explizit, auf 30 Minuten/10 MiB begrenzt und startet nach Prozessneustart nicht wieder. Die Hauptansicht bietet einen expliziten Export des typisierten Kanals. 500 JVM-Tests, 0 Fehler, Debug-Build PASS. Offen: End-to-End-Zielgerät-Nachweis des Share-Sheets und reale Reason-State-Beobachtung ohne sensible Ausgabe. | Zielgerät-Export und Reason-State-Sanitization im gemeinsamen Hardwaretest beobachten; alle Ergebnisse getrennt als PASS/FAIL/INCONCLUSIVE persistieren. |
| SSD-Endpfad | Reale SAF-Grant-Reakquisition, kameraunabhängiges Catch-up, Readback, Unterbrechung/Restart und Wiedererscheinen. | HD03–HD05 mit nutzbarem Hub/SSD; fehlende Topologie bleibt `HARDWARE_DEFERRED`. |

## Delta C — Zielgerätevalidierung des Kern-Backups

| Nachweis | Stand |
|---|---|
| Automatische bekannte Kamera, vollständiges Inventar, automatischer Transfer und sichtbarer Fortschritt | Teilweise beobachtet; PC20 blockiert den Abschluss. |
| Hintergrund/Sperrbildschirm, Power-Cycle und Ursache-Codes | Frühere Teilbeobachtungen vorhanden; auf finalem repariertem Build noch gebündelt zu validieren. |
| Kamera→Telefon-Integrität | Für neue Testdatei früher beobachtet; auf finalem Build nur bei PC20-Reparatur wiederholen, nicht geschützte Medien erneut übertragen. |
| Quelle-Continuity für Append/Resume | Ungeklärt; unsicheres Produktions-Resume bleibt deaktiviert. |

## Delta D — absichtlich gesperrte oder spätere Grenzen

| Bereich | Warum nicht autonom abschließbar |
|---|---|
| R-038/R-039 Kamera-Bereinigung | Reale Löschung benötigt eine separate, präzise destruktive Nutzerfreigabe und zuvor belegte Redundanz/Quellenidentität. Design/Fakes dürfen vorher weiterentwickelt werden. |
| R-043 Identität, Signing, Distribution, Produktions-OAuth | Benötigt Produktidentitätsentscheidung, Secrets/Accounts und Releasefreigabe; `release_allowed: false`. |
| Cloud-/Replica-Provider | Lokaler Backup-Erfolg bleibt cloudunabhängig; Produktionskonten und OAuth werden nicht implizit erzeugt. |

## Priorisierte Reihenfolge

1. PC20 terminale Inventarvollständigkeit reparieren und vollständig regressieren.
2. R-037 deterministische Capture-Day-/Pfad-/Gruppenmatrix schließen.
3. R-041 Diagnostik-Sinks/Export und R-040 GPS-Modusgrenzen implementieren und testen.
4. R-042 softwareseitige Liveness-Fault-Matrix schließen.
5. Einen erweiterten, nicht-destruktiven Hardwaretest für alle dann verbleibenden physischen Kriterien durchführen.
6. Erst nach expliziten separaten Freigaben: Bereinigung, Release/Signierung und Produktionsintegrationen.
