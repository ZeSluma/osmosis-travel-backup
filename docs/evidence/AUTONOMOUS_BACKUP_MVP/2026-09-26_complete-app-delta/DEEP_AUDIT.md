# Tiefenprüfung — 2026-09-26

Scope: `codex/autonomous-backup-mvp`, commit `27a105b`. Diese Prüfung trennt reproduzierbare
Softwareevidenz, statische Sicherheitsbefunde und noch nicht beobachtete Hardwarefakten. Sie
behauptet keine physische PASS-Evidenz.

## Lessons Learned als Prüfkriterien

Die wiederholten historischen Fehlerklassen wurden gegen die aktuelle Umsetzung geprüft:

1. Ersatzsitzungen dürfen nicht durch alte Plattform-Callbacks/STOPs beendet werden.
2. Ein kompletter Plan muss genau einen Writer erzeugen; unvollständige Inventare dürfen nie
   Arbeit oder Abschluss behaupten.
3. Fortschritt darf nur einen aktiven service-eigenen Worker repräsentieren und muss terminal
   verschwinden.
4. Telefon- und SSD-Beleg bleiben voneinander sowie von einer unbewiesenen Quellenäquivalenz
   getrennt.
5. Diagnose und UI dürfen keine Zugangsdaten, Netzwerk-/Medien- oder GPS-Daten offenlegen.
6. Jeder Kamera-Löschpfad muss während des aktuellen Produktumfangs fail-closed sein.

Die zugehörigen Policy-, Szenario-, Integritäts-, Liveness-, Privacy- und UI-Transition-Tests
sind im aktuellen Testreport vorhanden. `CameraCleanupPolicy.maySendDelete()` ist konstant
`false`; Sichtbarkeit und alle bekannten Ausführungspfade prüfen diese Sperre erneut.

## Reproduzierbare vorhandene Evidenz

- `app/build/test-results/testDebugUnitTest`: 544 Tests, 0 Failures, 0 Errors, 0 Skips;
  Zeitstempel 2026-09-26T15:55Z.
- `app/build/reports/tests/testDebugUnitTest/index.html`: 544 Tests, 100 % erfolgreich.
- Vorhandene Debug-APK SHA-256:
  `2AC036D1F3E205FA82005E13E669CB418052BD4FA6DF35A92CD3E3C8C31787D9`.
- Der Check deckt u. a. vollständige Inventarplanung, Writer-Fencing, Partial/Restart,
  Integritäts-/Readback-Regeln, Replica-Fehler, stale callback/epoch recovery, UI-Terminal- und
  Privacy-Zustände ab.

## Neuer Lauf in dieser Umgebung

Der komplette Befehl
`gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon` wurde
gestartet. Der Gradle-Wrapper wurde erfolgreich bezogen; der Lauf endete vor Kompilierung mit
`SDK location not found`. In dieser Arbeitsumgebung ist weder `ANDROID_HOME` noch ein Android SDK
vorhanden. Das ist ein Reproduzierbarkeitsblocker der Umgebung, kein fachlicher Testfehler und
kein Ersatz für die vorhandene Evidenz. `lintDebug` wurde deshalb ebenfalls nicht neu bestätigt.

### Fortsetzung: isolierte SDK-Wiederherstellung und Datenschutzbefund

Am 2026-09-26 wurde ein isolierter Workspace-Cache `.android-sdk/cmdline-tools/latest` mit den
offiziellen Android Command-line Tools eingerichtet. Die Installation von Platform API 36,
Build-Tools und Platform-Tools bleibt ausstehend, weil `sdkmanager` die Google-SDK-Lizenz verlangt.
Eine Annahme dieser Vertragsbedingungen im Namen des Nutzers wurde nicht vorgenommen und wartet
auf ausdrückliche Zustimmung.

Die Quellpfadprüfung fand zugleich eine Verletzung von INV-005 im Drone-Diagnosepfad:
`DroneSession` schrieb zuvor rohe Handshake-, empfangene Paket-, Query- und Beacon-Bytes in den
diagnostischen Log-Sink. Solche Bytes können Geräteidentität oder Medienmetadaten enthalten.
Die vier Ausgaben wurden auf reine Byteanzahl-/Statusangaben reduziert; die Transport- und
Protokolloperationen blieben unverändert. Zusätzlich hält `DumlTransport` nicht mehr das letzte
vollständige ausgehende Paket für Diagnosen vor, sondern nur noch dessen Byteanzahl. Statisch
geprüft: keine der vier früheren Hex-Ausgaben oder der Rohpaketpuffer sind mehr vorhanden,
`git diff --check` ist erfolgreich. `tools/autonomy/control.py --check` lief über die gebündelte
Workspace-Python-Laufzeit und ergab `CONTINUE` / `USEFUL_SOFTWARE_WORK_REMAINS`; der
Hardware-Batch ist folgerichtig nicht bereit. Dieser Fix benötigt noch den frischen
JVM-/Build-/Lint-Checkpoint, sobald die SDK-Lizenz und API-36-Pakete autorisiert sind.

## Ergebnis der Anforderungsprüfung

Die deterministischen Kernpfade sind **SOFTWARE_PROVEN**, jedoch ist die Software nicht zu
100 Prozent als gesamtes Produkt verifiziert:

- HD01/HD02: reales S25/Pocket-Verhalten für automatische Auswahl, nicht-leeres Inventar,
  Hintergrund und Wiederherstellung ist noch hardwareabhängig.
- HD03–HD05: reale USB-Hub-/SSD-/SAF-Reacquisition, Readback, Unterbrechung und Catch-up sind
  hardwareabhängig; die Topologie war zuletzt nicht nutzbar.
- R037, R040, R041, R042 und R043 haben deterministische Softwarebelege, brauchen aber noch die
  jeweilige reale Zielgerätebeobachtung.
- R009/OneDrive ist eine offene Produkt-/Tenantentscheidung, nicht implementiert oder durch
  einen lokalen Backupdurchlauf ersetzbar.
- Unveränderliche Pocket-Quellenkontinuität für produktives Append/Resume bleibt absichtlich
  unqualifiziert; die App verweigert diesen unsicheren Pfad.

Die kombinierte vollständige, nicht-destruktive Hardwareliste steht in
`docs/hardware/COMPLETE_FUNCTION_HARDWARE_AUDIT_2026-09-26.md`.
