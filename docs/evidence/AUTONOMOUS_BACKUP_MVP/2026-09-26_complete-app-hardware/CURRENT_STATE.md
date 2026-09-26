# Konsolidierter Projektstand — 2026-09-26

## Sicherheits- und Produktgrenzen

- Kameraoriginale wurden nie gelöscht; `Safe to Clear` bleibt rein informativ.
- Keine App-Daten, lokalen Medien oder SSD-Inhalte wurden zurückgesetzt, formatiert oder überschrieben.
- Upstream und `main` blieben unverändert. Alle Änderungen liegen auf
  `codex/autonomous-backup-mvp`.
- Diagnosen enthalten nur bereinigte Zustände/Zähler; keine Medieninhalte, Zugangsdaten,
  Netzwerkkennungen oder GPS-Daten.

## Software, die nachweislich vorhanden ist

1. Service- und Coordinator-eigene Kamera-/Session-/Transfer-Ownership mit Epoch-/Generations-
   Fences gegen verspätete BLE-, Netzwerk-, Datalink- und STOP-Callbacks.
2. Vollständigkeits- und Vertrauensgrenzen: unvollständige oder leere Inventuren bleiben
   untrusted; bekannte Assets werden daraus nicht als gelöscht abgeleitet; automatische Arbeit
   setzt eine sichere aktuelle Beobachtung voraus.
3. Dauerhafte Planung, strikter Einzelschreiber, Teilzustands-/Prozess-Neustart-Schutz,
   lokale Integritätsbelege sowie konservative Kamera-Sync-, SSD-Redundanz- und
   Safe-to-Clear-Ableitung.
4. Persistente SAF-/SSD-Replica-Architektur einschließlich Catch-up ohne aktive Kamera; reale
   USB-/Hub-/Provider-Eigenschaften bleiben ausdrücklich physisch zu validieren.
5. UI beobachtet durable/service-eigene Projektionen. Sie kann weder eine Sitzung noch einen
   Writer besitzen oder eine Vollständigkeits-/Verifikationsbehauptung erzeugen.

## Beobachtete S25/Pocket-Evidenz

- Gespeicherte Kamera verband in mehreren Durchläufen automatisch; Grid-Aufbau und konservative
  Zustände erschienen ohne Medienmanipulation.
- Ein neuer unkritischer Clip wurde service-eigen ohne Download-Taste übertragen und erhielt eine
  lokale Integritätsbestätigung.
- Hintergrund/Sperrbildschirm überstand die Sitzung; ein kontrollierter Kamera-Power-Cycle stellte
  die Verbindung in beobachteten Durchläufen automatisch wieder her.
- Frühere Fehler wurden nicht kaschiert: ein hängender Vorbereitungsbalken und ein verspäteter
  epochloser STOP wurden als Softwarefehler erfasst, repariert und testbar gemacht.
- Der jüngste Zielgerätebefund bestätigte die terminale Inventurkopie. Ein App-Start bei bereits
  eingeschalteter Kamera benötigte in diesem Einzelfall einen Kamera-Power-Cycle; das bleibt
  getrennt als `INCONCLUSIVE` für den Startpfad, nicht als behaupteter PASS.

## Aktuelle UI-Regeln

- Terminal unvollständige Inventur: **„Kamera-Sicherung wartet auf vollständige Dateiliste“**.
  Das bedeutet: keine neue automatische Übertragung und keine Freigabe, bis die Quelle vollständig
  und sicher erfasst ist.
- Aktive Planung, Übertragung und Integritätsprüfung erhalten eine sichtbare Fortschrittsanzeige.
- Erfolg bleibt kurz sichtbar und wird automatisch ausgeblendet. Terminale Review-, No-work- und
  Source-changed-Zustände lassen keinen laufend wirkenden Balken zurück.

## Test- und Prozessverschärfung

`docs/EXECUTION_POLICY.md` verlangt ab jetzt für jede geänderte sichtbare Anzeige eine
deterministische Matrix: Eintritt, Fortschritt, erfolgreicher Terminalzustand, fail-closed
Terminalzustand und verspäteter/ersetzter Callback. Jede Zeile muss sowohl falschen Abschluss als
auch falsche Aktivität ausschließen. Die zuletzt ergänzten gezielten Tests sind grün:

- `BackupStatusCopyTest`: 8 Tests, 0 Fehler.
- `AutomaticTransferUiStatePolicyTest`: 3 Tests, 0 Fehler.

Die vollständige Software-Abschlussmatrix wurde deshalb bewusst wieder geöffnet: die erweiterte
Übergangsmatrix muss vor einem erneuten Gesamt-Hardwaretest vollständig auditiert werden.

## Verbleibende physische Validierung

Die gebündelte, nicht-destruktive S25/Pocket-Prüfung steht in
`docs/hardware/FINAL_COMPLETE_APP_HARDWARE_TEST.md`. Sie umfasst Verbindungsstart,
Vollständigkeits-/Ruheanzeige, neuen Testclip mit Fortschritt/Integrität, Hintergrund und
Power-Cycle. SSD-Schritte bleiben zusammen `HARDWARE_DEFERRED`, solange die Dongle-/SSD-Topologie
nicht zuverlässig verfügbar ist. Kein physischer PASS wird aus JVM-/Emulator-Evidenz abgeleitet.
