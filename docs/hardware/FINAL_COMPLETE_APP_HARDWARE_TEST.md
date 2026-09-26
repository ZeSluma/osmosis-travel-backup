# Finaler Gesamt-Hardwaretest — Osmosis Travel Backup

Status: READY — die verpflichtende sichtbare Übergangsmatrix ist softwareseitig auditiert. Dieser Ablauf validiert reale S25/Pocket/SSD-Eigenschaften;
eine nicht beobachtbare Bedingung ist `INCONCLUSIVE`, niemals `PASS`.

## Build unter Test

- Branch: `codex/autonomous-backup-mvp` (the commit carrying this plan and the Pocket Pickup
  resource set is the exact source under test)
- Lokale Debug-APK: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `B5D856F25976D76050935463CED59A8E312AFE22ED7CF4D94BECD18FA340E66B`
- Interne Grundlage: 531 JVM-Tests, 0 Fehler/Errors; vollständige sichtbare Übergangsmatrix in `UI_TRANSITION_AUDIT.md` plus Pocket-Pickup-Ressourcenprüfung. Die Installation erfolgt ausschließlich **in place**;
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
| 0 | Nach der In-place-Installation nur Launcher und Startoberfläche ansehen; anschließend die normale App-Sitzung fortsetzen. | R043 | Name **Pocket Pickup**, Petrol-Taschenzeichen, lesbarer heller bzw. dunkler Kontrast. Keine Datenmigration oder neue Berechtigungsanfrage. | Falscher Name/Icon oder unlesbarer Kontrast = FAIL. |
| 1 | Mit der bereits installierten APK die App öffnen; Pocket einschalten und **nicht** "Neu suchen" oder manuell verbinden. | HD01, PC01/PC17 auf Zielgerät | Genau eine automatische Auswahl/Verbindung und Grid ohne Rescan. | Falsche/doppelte Sitzung = FAIL; fehlendes Gerät = INCONCLUSIVE. |
| 2 | Inventar und Ruheansicht abwarten. | HD01, PC02–PC04, PC20 | Eine abgesetzte Karte „Sicherungsstatus“; bei aktuell unvollständiger Liste: „Kameraliste noch nicht vollständig – deshalb keine Übertragung“. Bei nur historischen Prüffällen: „Frühere Dateien brauchen Prüfung (N) – neue Dateien werden weiterhin gesichert“. Kein alter technischer Vierzeiler und kein dauerhaft sichtbarer Fortschrittsbalken ohne Arbeit. | Leeres/unvollständiges Inventar wird fälschlich vertraut, historische Prüffälle widersprechen neuer Sicherung, oder Anzeige bleibt nach Abschluss aktiv = FAIL. |
| 3 | Einen neuen, sicheren Testclip aufnehmen; App öffnen, keine Download-Taste drücken. | HD01, PC05–PC08, PC18 | Service startet genau einen Download; Fortschritt zeigt Vorbereitung → Übertragung → Integritätsprüfung → Abschluss; danach lokaler Integritätsbeleg, aber keine falsche Quellen-/Redundanz-/Löschfreigabe. | Doppelwriter/falscher Abschluss/stehender Fortschritt = FAIL. |
| 4 | App während Leerlauf oder Transfer in Hintergrund/Sperrbildschirm geben, nach 15 Sekunden zurückholen. | HD01, HD02, PC12/PC18 | Wahrer Sitzungs-/Transferstatus bleibt erhalten; UI zeigt den aktuellen Zustand. | Verlust/Duplikat/falscher Status = FAIL. |
| 5 | Pocket 15 Sekunden ausschalten und wieder einschalten; **nicht** rescanen oder manuell verbinden; bis 60 Sekunden warten. | HD01, HD02, R-042 Teilnachweis | Begrenzte automatische Wiederherstellung plus neue Quellenvalidierung; kein verspäteter STOP beendet die Ersatzsitzung; nach terminalem Ergebnis verschwindet der Fortschrittsbereich. | Umgehung der Revalidierung, keine Wiederherstellung oder hängende Anzeige = FAIL. |
| 6 | Nur die angezeigten Zustandsgründe prüfen, keine Rohlogs exportieren. | HD02, PC13 | Ursache bleibt korrekt oder UNKNOWN/USER_ACTION_REQUIRED; keine sensiblen Daten. | Spekulative Ursache oder sensible Daten = FAIL. |
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
