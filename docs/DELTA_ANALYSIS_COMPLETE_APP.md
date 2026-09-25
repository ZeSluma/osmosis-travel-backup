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
| PC20 | Nach automatischer Verbindung und sieben sichtbaren Videos bleibt `Automatisch: Kameraliste wird noch geprüft`. | Terminales Datalink-Inventar bis `LedgerCoordinator`/Planprojektion verfolgen; fehlende oder verlorene Vollständigkeitsinformation korrigieren; deterministische Regression ergänzen. | JVM-/Integrations-Regression, Debug-Build, dann eine Wiederholung von HD01 ohne Rescan. |

## Delta B — vollständige nicht-destruktive Produktfunktionen

| Bereich | Offene Arbeit | Test-/Abschlusskriterium |
|---|---|---|
| R-037 Capture-Day | CD01–CD08 als integrierte Matrix schließen: stabile Tagesordner, Jahreswechsel, Quellenpriorität/Unsicherheit, eingefrorene Pfade über Neustart, Sidecar-Gruppen, Konflikte und Replica-Pfade. Reale Pocket-Zeit-/Zeitzonen-/MIME-Fakten fehlen. | JVM/Emulator für alle deterministischen Regeln; gebündelte S25/Pocket-Beobachtungen für Zeitquelle, Mitternacht, Sidecars und Zielordner. |
| R-042 Liveness | KA01–KA07: Idle-/Aktivtransfer-Verhalten, Screen-off, kontrollierter Verlust, begrenzte Wiederherstellung und Ursache-Unsicherheit. Keine unbelegte Keepalive- oder Power-Setting-Behauptung. | Service-/Fault-Tests plus ein realer, zeitlich erfasster S25/Pocket-Liveness-Test. |
| R-040 GPS-Sync | Opt-in-GPS-Lebenszyklus, Berechtigungs-/Moduskonflikt mit Backup, keine stille Wiederaufnahme und keine Beeinträchtigung von Backup bei GPS aus/abgelehnt. | Unit/Instrumentation für Berechtigungs-/Arbiterzustände; echte opt-in-Hardwarevalidierung nur mit separater Nutzerfreigabe. |
| R-041 Diagnostik | Typisierte, begrenzte, bereinigte Eventablage/Export sowie Fehler-/Speichergrenzen implementieren oder bestehende Logpfade endgültig gegen die Spezifikation absichern. | Sink-/Privacy-Tests einschließlich Export und Größen-/Zeitgrenzen; reale Reason-State-Beobachtung ohne sensible Ausgabe. |
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
3. R-041 Diagnostik-Speicher/Export und R-040 GPS-Modusgrenzen implementieren und testen.
4. R-042 softwareseitige Liveness-Fault-Matrix schließen.
5. Einen erweiterten, nicht-destruktiven Hardwaretest für alle dann verbleibenden physischen Kriterien durchführen.
6. Erst nach expliziten separaten Freigaben: Bereinigung, Release/Signierung und Produktionsintegrationen.
