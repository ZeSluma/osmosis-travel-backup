# Hardware-Test V2 — vollständige, nicht-destruktive Produktvalidierung

Status: **BEREIT ZUR AUSFÜHRUNG**. Dieser Ablauf ersetzt keine Sicherheitsgrenzen und ist der
einzige nächste physische Test. Er bündelt die noch offenen S25-/Pocket-/UI-/SSD-Nachweise, statt
einzelne Symptome mehrfach zu testen.

## Schutzgrenzen

- Aktuelle Debug-APK ausschließlich *in place* installieren; keine App-Daten löschen oder
  zurücksetzen. Referenz: SHA-256
  `C39082CE3674DEB70548BB7C8D22F23E1080C6454187EFBF7EAC57B50BE43B08`.
- Keine Kameraoriginale löschen, formatieren oder geschützte Altdateien erneut herunterladen.
- Nur einen neu aufgenommenen, unkritischen Testclip verwenden.
- SSD nur mit leerem dedizierten Testordner; keine bestehenden SSD-Dateien verändern.
- Nur Zustände, Zähler und Zeitdauern dokumentieren – keine Namen, Medieninhalte, Zugangsdaten,
  Netzwerkdetails oder GPS-Werte.

## Ablauf und Orakel

| Schritt | Aktion | PASS | FAIL / INCONCLUSIVE |
|---|---|---|---|
| 1 – Start/Design | APK in place installieren, App mit ausgeschaltetem Pocket öffnen, Pocket innerhalb von 30 s einschalten. | **Pocket Pickup** ist lesbar; genau eine automatische Verbindung; keine unerwartete Berechtigung oder Datenmigration. | Keine Verbindung oder doppelte Sitzung = `FAIL`; Geräteproblem = `INCONCLUSIVE`. |
| 2 – Inventar/Ruhe | Nach dem Raster 60 s ohne Aktion warten. | Vollständige Liste wird als vollständig gezeigt; bei unvollständiger Liste steht klar, dass **keine Übertragung** startet. Eine offene historische Prüfung ist kein laufender Worker. Raster/Filter bleiben ruhig. | Hängender „läuft“-Text, falscher Abschluss oder Flackern = `FAIL`. |
| 3 – Neuer Clip | App schließen, einen neuen kurzen Clip aufnehmen, App öffnen; keine Download-Taste drücken. | Genau ein automatischer Ablauf: Vorbereitung → messbarer Fortschritt oder ehrlicher Kurztransfer → Integritätsabschluss. Kachel und Karte stimmen zusammen; nach Abschluss bleibt kein aktiver Balken zurück. | Doppelwriter, falscher/alter Text, Dauerbalken oder Flackern = `FAIL`. |
| 4 – Hintergrund | Bei Ruhe oder aktivem sicheren Ablauf App 15 s in Hintergrund/Sperre, dann zurück. | Sitzung und UI zeigen denselben aktuellen Zustand; kein zweiter Writer, kein veralteter Fortschritt. | Verlust, Duplikat oder falscher Zustand = `FAIL`. |
| 5 – Power-Cycle | Pocket 15 s ausschalten, dann einschalten und 60 s abwarten; weder Rescan noch manuelles Verbinden. | Begrenzte automatische Wiederherstellung, Revalidierung vor Datenverkehr, danach ehrliches Inventar/Status. | Keine Wiederherstellung, unvalidierter Transfer oder hängender Aktivstatus = `FAIL`. |
| 6 – Gründe/Privatsphäre | Während Schritt 4/5 sichtbare Gründe und optionale Diagnoseansicht kontrollieren. | Nur nachvollziehbare oder `USER_ACTION_REQUIRED`/unbekannte Ursache; keine sensiblen Daten. | Spekulative Ursache oder sensible Daten = `FAIL`. |
| 7 – SSD, nur falls verfügbar | Nach einem nachweislich lokalen Clip Pocket trennen, zuvor freigegebenen leeren SSD-Ordner verbinden; einmal SSD trennen/erneut verbinden. | SSD wird ohne neue Ordnerauswahl korrekt als verfügbar/unverfügbar erkannt; Catch-up nur mit sicherem Readback; Unterbrechung bleibt PARTIAL/Review. | Falsche Schreibbarkeit, Überschreiben, Redundanz ohne Readback = `FAIL`; keine nutzbare Topologie = `HARDWARE_DEFERRED`. |

## Ergebnisregeln

- Ein sicherheitsrelevanter Fehler beendet den Test sofort; ansonsten werden unabhängige Schritte
  weitergeführt und am Ende gemeinsam ausgewertet.
- Ein nicht verfügbarer physischer Zustand ist `INCONCLUSIVE` beziehungsweise
  `HARDWARE_DEFERRED`, niemals ein erfundener PASS.
- Jeder FAIL eröffnet wieder eine Softwarephase mit Reproduktion an derselben
  Entscheidungsgrenze, Regressionstest und erst danach einem neuen gebündelten Durchlauf.

## Abgedeckte Restanforderungen

Dieser Test bündelt HD01/HD02 sowie HF01–HF08; Schritt 7 deckt – nur bei verfügbarer Hardware –
HD03–HD05 und HF13–HF15 ab. Capture-Day, GPS-Opt-in und Diagnoseexport bleiben Teil des
vollständigen Referenzplans `COMPLETE_FUNCTION_HARDWARE_AUDIT_2026-09-26.md`, falls sie in
derselben Sitzung ohne zusätzliche Risiko- oder Zeitbelastung geprüft werden können.
