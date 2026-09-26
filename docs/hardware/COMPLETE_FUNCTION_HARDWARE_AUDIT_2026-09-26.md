# Vollständige Hardware-Prüfung — Pocket Pickup

Status: **READY NACH SOFTWARE-CHECKPOINT, nicht ausgeführt**. Dieser Plan ergänzt den bestehenden
[`FINAL_COMPLETE_APP_HARDWARE_TEST.md`](FINAL_COMPLETE_APP_HARDWARE_TEST.md): Er enthält auch
GPS, Diagnose, Capture-Day und die Sicherheits-Sperren. Ein Ergebnis ist ausschließlich
`PASS`, `FAIL`, `INCONCLUSIVE` oder — bei fehlender Topologie — `HARDWARE_DEFERRED`.

## Feste Schutzgrenzen

- Die signer-kompatible Debug-APK ausschließlich *in place* installieren. Niemals App-Daten
  löschen, zurücksetzen oder eine vorhandene Referenzdatei überschreiben.
- Nur einen neu aufgenommenen, unkritischen Testclip verwenden. Keine Kamera-Originale löschen,
  keine Kamera formatieren und keine Recovery-/Resume-Experimente mit geschützten Dateien.
- SSD nur in einem neuen leeren Testordner verwenden. Keine bestehenden SSD-Dateien umbenennen,
  überschreiben oder löschen.
- Nur bereinigte Zustände, Zähler und Zeitdauern notieren: nie Zugangsdaten, MAC/SSID/IP,
  Dateinamen, Medieninhalt, GPS-Werte, Rohpakete oder Rohlogs.

## Vorbedingungen

1. Der aktuelle Branch einschließlich der Diagnose-Privatsphäre-Korrektur hat vorher einen frischen
   `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug`-Checkpoint bestanden; der SHA-256 der
   daraus entstandenen Debug-APK ist im Ergebnisprotokoll festgehalten. Ohne diesen Nachweis findet
   kein Hardwaretest statt.
2. S25, gespeicherter Pocket und die getestete APK sind verfügbar. Akku und freier Speicher sind
   ausreichend.
3. Für SSD-Fälle: funktionsfähiger USB-Hub, SSD und ein leerer dedizierter Testordner. Fehlt
   diese Topologie, werden SSD-Fälle gemeinsam `HARDWARE_DEFERRED`, ohne Pocket-Fälle zu
   wiederholen.
4. Vorhandene lokale Belege/Teilübertragungen nur lesend notieren. Vor dem Start muss keine
   Bereinigung stattfinden.

## Ein zusammenhängender, nicht-destruktiver Durchlauf

| ID | Handlung | Erwartetes PASS-Kriterium | Bei Abweichung |
|---|---|---|---|
| HF01 Branding/Start | APK in place installieren, Launcher und Startansicht bei hellem/dunklem System ansehen. | Name **Pocket Pickup**, Icon und Kontrast lesbar; keine Datenmigration oder neue unerwartete Berechtigung. | Sicht-/Identitätsfehler: `FAIL`. |
| HF02 Gespeicherte Kamera | App mit ausgeschaltetem Pocket öffnen, Pocket innerhalb von 30 s einschalten — weder Rescan noch manuelles Verbinden. | Genau eine Auswahl/Verbindung und ein nicht-leeres, vollständiges Inventar; keine doppelte Sitzung. | Gerät nicht verfügbar: `INCONCLUSIVE`; Doppel-/Fehlsitzung: `FAIL`. |
| HF03 Inventar/Status | Nach der Inventarisierung Ruheansicht prüfen, auch bei bekannter historischer Unsicherheit. | Unvollständige aktuelle Liste blockiert automatische Arbeit; offene Beweise sind nicht als laufende Arbeit dargestellt; kein flackerndes Raster/kein dauerhafter Fortschrittsbalken ohne Worker. | Falsches Vertrauen, falsche Aktivität oder flackernde Projektion: `FAIL`. |
| HF04 Automatischer Telefontransfer | Einen neuen Testclip aufnehmen, App öffnen, keine Download-Taste drücken. | Genau ein service-eigener Transfer: Vorbereitung → sichtbarer Fortschritt → Integritätsabschluss; danach kein Quellen-, Redundanz- oder Lösch-Overclaim. | Doppelwriter, falscher Abschluss, hängender/nachlaufender Fortschritt: `FAIL`. |
| HF05 Kurzzeitige Pocket-Antwortstörung | Nur wenn vor dem ersten Byte natürlich beobachtbar: transienten 404/500-Zustand abwarten; nicht künstlich wiederholen. | Sichtbarer begrenzter Retry, danach messbarer Fortschritt oder dauerhafter Review; keine Zieldatei vor erfolgreichem Start. | Unbegrenzter/stiller Retry oder falscher Abschluss: `FAIL`; nicht beobachtbar: `INCONCLUSIVE`. |
| HF06 Hintergrund/Lebenszyklus | App bei Leerlauf oder Transfer für 15 s in Hintergrund/Sperrbildschirm, dann zurückholen. | Wahrer Sitzungs-/Transferzustand bleibt; kein neuer Writer und keine falsche UI. | Verlust, Duplikat oder falscher Zustand: `FAIL`. |
| HF07 Kontrollierter Pocket-Verlust | Pocket 15 s ausschalten, dann wieder einschalten; kein Rescan/manuelles Verbinden, bis 60 s warten. | Begrenzte automatische Wiederherstellung, Quellenrevalidierung vor Verkehr, Ersatzsitzung wird nicht von altem STOP beendet. | Keine Wiederherstellung, unvalidierter Verkehr oder hängender Aktivstatus: `FAIL`. |
| HF08 Gründe und Privatsphäre | Während HF06/HF07 nur sichtbare Gründe und bereinigte Diagnosen prüfen. | Ursache ist korrekt oder `UNKNOWN`/`USER_ACTION_REQUIRED`; keine Geheimnisse, Netzwerk-/Medien-/GPS-Daten sichtbar. | Spekulative Ursache oder sensible Daten: `FAIL`. |
| HF09 Capture-Day | Testclip-Tag, angezeigten logischen Tag und Herkunftsunsicherheit nur ablesen. | Tag und Unsicherheit bleiben erhalten; keine unbelegte UTC-/Zeitzonenbehauptung. | Stärkere falsche Behauptung: `FAIL`; keine zulässige Zeitquelle: `INCONCLUSIVE`. |
| HF10 GPS-Opt-in | Ohne GPS-Modus starten; erst danach GPS ausdrücklich aktivieren, Berechtigung bewusst erlauben/ablehnen; Modus abschalten und App neu öffnen. | Frischer Start bleibt Backup-Modus; Standortzugriff nur nach aktueller Zustimmung; nach Abschalten/Neustart kein stilles GPS und kein zweiter BLE-Owner. | Automatisches GPS, falscher Berechtigungsfluss oder blockierter Backupbetrieb: `FAIL`. |
| HF11 Diagnose-Export | Mit einer normalen Sitzung die explizite Diagnose-/Exportfunktion öffnen; Export optional abbrechen. | Export ist bewusst ausgelöst, begrenzt und ohne Geheimnisse, GPS, Pfade, Namen oder Rohfehler; Backupzustand bleibt unverändert. | Automatischer Export/Leak/Einfluss auf Backup: `FAIL`. |
| HF12 Löschsperre | Auswahlmodus mit Testclip ansehen; keine Löschbestätigung ausführen. | Lösch-Schaltfläche bleibt verborgen bzw. jeder Auslösepfad verweigert mit `CAMERA_CLEANUP_NOT_AUTHORIZED`; keine Kamera-Schreiboperation. | Erreichbare Löschbestätigung oder Schreiboperation: `FAIL` und Test stoppen. |
| HF13 SSD-Grant/Reacquisition | Nur mit SSD: leeren Ordner einmal autorisieren, Pocket trennen, SSD/HUB trennen/neu verbinden, App neu starten. | Persistenter Grant/Zielpfad wird nur bei gültigem Zugriff erkannt; keine Picker-Pflicht, kein Schreiben bei fehlendem Grant. | Falsche Schreibbarkeit, neuer Picker oder Überschreiben: `FAIL`; Topologie fehlt: `HARDWARE_DEFERRED`. |
| HF14 SSD-Catch-up/Readback | Mit verifiziertem Telefonbeleg und getrenntem Pocket SSD wieder anstecken. | Automatisches Catch-up im gespeicherten Pfad, unabhängiger Readback; UI aktualisiert sich ohne App-Neustart. | Keine Wiederaufnahme, falscher Pfad oder Redundanz ohne Readback: `FAIL`. |
| HF15 SSD-Unterbrechung | Zweite Testkopie kontrolliert durch SSD-Abziehen unterbrechen, einmal App neu starten und SSD wieder anstecken. | Unterbrochene Replica bleibt `PARTIAL`/Review, keine doppelte finale Datei und keine Redundanzbehauptung. | Teilkopie als fertig, Duplikat oder Integritätsüberhöhung: `FAIL`. |
| HF16 Quellenkontinuität | Nur read-only, wenn eindeutige Metadaten ohne neue Übertragung sichtbar sind. | Nur eindeutig belegter unveränderlicher Quellenvertrag würde Resume qualifizieren; sonst sichere Resume-Verweigerung. | Widersprüchliche Semantik: `FAIL`; keine eindeutige Metadatenbasis: `INCONCLUSIVE`. |

## Nicht als Hardwaretest ersetzbar

- **R009 / OneDrive:** Es existiert keine autorisierte Tenant-/Produktentscheidung und keine
  Cloud-Konfiguration. Das ist `PRODUCT_DECISION_DEFERRED`, nicht als Kamera-/SSD-Test zu
  simulieren.
- **Kamera-Bereinigung:** Die Funktion ist bewusst gesperrt; ein echter Löschtest ist ohne
  separate explizite Autorisierung verboten. HF12 prüft ausschließlich die Sperre.
- Ein `PASS` eines Einzeltests verleiht weder Löschfreigabe noch Release-Freigabe und ersetzt
  keine getrennte Quellenäquivalenz-/Resume-Evidenz.

## Ergebnisprotokoll

Zu jeder ID werden nur Ergebnis, beobachteter bereinigter Zustand und Zeitpunkt festgehalten.
Ein `FAIL` hält die übrigen sicheren, unabhängigen Fälle nicht auf; ein sicherheitsrelevanter
Fehler bei HF12 beendet dagegen die Sitzung sofort. Anschließend erfolgt eine gemeinsame,
evidenzbasierte Fehleranalyse statt wiederholter Einzelfall-Rescans.
