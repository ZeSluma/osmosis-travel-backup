# Finaler Gesamt-Hardwaretest — Osmosis Travel Backup

Status: READY, noch nicht durchgeführt. Dieser Ablauf validiert reale S25/Pocket/SSD-Eigenschaften;
eine nicht beobachtbare Bedingung ist `INCONCLUSIVE`, niemals `PASS`.

## Build unter Test

- Branch/Commit: `codex/autonomous-backup-mvp` / `251ab516f22e63e3a600e0b5cf2450333f2a7bee`
- Lokale Debug-APK: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `E09FD05B34E18F6393F0AE04D26823B781016B291D2932F8A49EA32ADC7878FA`
- Interne Grundlage: 523 JVM-Tests, 0 Fehler. Die Installation erfolgt ausschließlich **in place**;
  vorhandene App-Daten bleiben erhalten.

## Schutzgrenzen

- Aktuelle signer-kompatible Debug-APK nur **in place** installieren; keine App-Daten löschen.
- Keine Kamera-Originale löschen, keine geschützten Dateien erneut herunterladen, keine SSD formatieren
  und keine vorhandenen SSD-Dateien überschreiben.
- Ausschließlich ein neuer, nicht kritischer Testclip darf übertragen werden. SSD-Arbeit nur in einem
  leeren, eigens angelegten Testordner.
- Diagnose nur als bereinigte Zustände, Zähler und Zeiten erfassen: keine Zugangsdaten, MAC/SSID/IP,
  GPS-Koordinaten, Dateinamen, Medieninhalte oder Rohpakete.

## Voraussetzungen

1. S25, gespeicherter Pocket und die oben genannte APK sind verfügbar; Akku und Speicher reichen aus.
2. Für den optionalen SSD-Zweig: nutzbarer Hub, SSD und leerer Testordner. Fehlt dies, HD03–HD05
   werden gemeinsam `HARDWARE_DEFERRED`, ohne den Pocket-Teil zu wiederholen.
3. Bestehende lokale Integritätsbelege und Teilübertragungen werden vor dem Start nur lesend notiert.

## Ein Ablauf, alle Kriterien

| Schritt | Aktion | Validiert | PASS | FAIL / INCONCLUSIVE |
|---|---|---|---|---|
| 1 | APK in place installieren; App öffnen, während Pocket aus ist; Pocket innerhalb von 30 Sekunden einschalten. | HD01, PC01/PC17 auf Zielgerät | Genau eine automatische Auswahl/Verbindung ohne Rescan; kein alter Callback verändert die neue Sitzung. | Falsche/doppelte Sitzung = FAIL; fehlendes Gerät = INCONCLUSIVE. |
| 2 | Vollständiges nicht-leeres Inventar abwarten. | HD01, PC02–PC04, PC20 | Grid und deutsche Zusammenfassung sind dauerhaft konsistent; offene historische Zuordnung bleibt erklärt, nicht als Abschluss dargestellt. | Leeres/unvollständiges Inventar wird fälschlich vertraut = FAIL; Inventar nicht verfügbar = INCONCLUSIVE. |
| 3 | Einen neuen, sicheren Testclip aufnehmen; keine Download-Taste drücken. | HD01, PC05–PC08, PC18 | Service startet genau einen Download; sichtbarer Fortschritt aktualisiert sich; danach lokaler Integritätsbeleg, aber keine falsche Quellen-/Redundanz-/Löschfreigabe. | Doppelwriter/falscher Abschluss = FAIL; kein sicherer Testclip = INCONCLUSIVE. |
| 4 | App in Hintergrund/Sperrbildschirm, dann zurückholen. | HD01, HD02, PC12/PC18 | Wahrer Sitzungs-/Transferstatus bleibt erhalten; UI ist Beobachter. | Verlust/Duplikat/falscher Status = FAIL. |
| 5 | Kontrollierter Pocket-Power-Cycle; keine Daten löschen. | HD01, HD02, R-042 Teilnachweis | Begrenzte Wiederherstellung plus neue Quellenvalidierung; Teilzustände bleiben konservativ. | Umgehung der Revalidierung = FAIL; nicht sicher induzierbar = INCONCLUSIVE. |
| 6 | Bereinigte Reason-State-Ausgabe prüfen. | HD02, PC13 | Ursache bleibt korrekt oder UNKNOWN/USER_ACTION_REQUIRED; keine sensiblen Daten. | Spekulative Ursache oder sensible Daten = FAIL. |
| 7 | **Nur bei SSD-Topologie:** Pocket trennen, einmal leeren SSD-Ordner autorisieren, SSD entfernen/neu verbinden, App neu starten. | HD03, HD05 | Vorheriger Grant und Zielpfad werden sicher wiedererkannt; Catch-up ohne Pocket/Picker. | Neuer Picker nötig, Schreiben bei fehlendem Grant oder Überschreiben = FAIL; Topologie fehlt = HARDWARE_DEFERRED. |
| 8 | **Nur bei SSD-Topologie:** eine replizierte Testdatei prüfen, zweite Kopie kontrolliert unterbrechen, einmal neustarten und SSD wieder verbinden. | HD04 | Fertige Replica hat unabhängigen Readback; Unterbrechung bleibt PARTIAL/review und erzeugt keine doppelte finale Datei. | Falscher Abschluss/Duplikat/Checksum-Behauptung = FAIL. |
| 9 | Read-only Capture-Day-Beobachtung: Testclip-Tag, angezeigter logischer Tag und Herkunftsunsicherheit notieren. | R-037 Teilnachweis | App bewahrt Tag/Herkunftsunsicherheit; keine Behauptung über UTC/Zeitzone ohne Beleg. | Falsche stärkere Behauptung = FAIL; keine zulässige Zeitinformation = INCONCLUSIVE. |

## Abschlussregel

Jeder Schritt erhält separat `PASS`, `FAIL`, `INCONCLUSIVE` oder bei fehlender SSD-Topologie
`HARDWARE_DEFERRED`. Nur beobachtete PASS zählen. Eine erfolgreiche Übertragung autorisiert weder
Kamera-Löschung noch Release. Vollständige R-037-Randfälle, optionale GPS-Funktionen und jede
destruktive Bereinigung bleiben eigene, später explizit autorisierte Testprogramme.

Zuordnung zur Queue: HD01/HD02 entsprechen `G7-LIFECYCLE-RECOVERY` und
`G7-REASON-DIAGNOSTICS`; HD03–HD05 entsprechen den drei `MVP-SSD-*`-Einträgen in
`VALIDATION_QUEUE.json`.

## Ergebnisprotokoll

Nach dem Durchlauf wird für jeden Schritt ausschließlich `PASS`, `FAIL`, `INCONCLUSIVE` oder
`HARDWARE_DEFERRED` samt beobachtetem Zustand und Zeitpunkt festgehalten. Keine Rohlogs,
Dateinamen, Zugangsdaten, GPS-Werte oder Medieninhalte dokumentieren. Ein einzelnes FAIL beendet
nicht die übrigen sicheren Schritte; erst danach folgt gesammelt die Fehleranalyse.
