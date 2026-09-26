# Gesamt-Hardwaretest — 2026-09-26

Status: IN PROGRESS. Nur beobachtete Ergebnisse zählen; keine Mediennamen, Inhalte,
Zugangsdaten oder Rohdiagnosen werden festgehalten.

| Schritt | Ergebnis | Beobachtung |
|---|---|---|
| 1 — bekannte Kamera aus → App starten → Kamera einschalten | PASS | S25 erkannte den gespeicherten Pocket und verband automatisch ohne manuelle Auswahl oder Rescan. |
| 2 — Inventar/Status | PASS (konservativer Status) | Raster wurde geladen. Die Zusammenfassung zeigte weiterhin Kameraabgleich/SSD-Sicherung offen und Sicher zum Löschen Nein, weil drei frühere Zuordnungen geprüft werden müssen. Keine falsche Abschlussbehauptung. |
| 3 — neuer unkritischer Testclip, App-Prozess schließen/neu öffnen | PASS | Nach automatischer Wiederverbindung zeigte die App `Automatisch: Transfer 0 Prozent, 0 von 1` mit Fortschrittsbalken. Der service-eigene Transfer lief ohne Download-Taste an und meldete anschließend Abschluss sowie aktualisierte Integritätsbelege/Dateistatus. Die Zusammenfassung kehrte konservativ zu den drei früheren Prüfungen zurück. |
| 4 — Hintergrund/Sperrbildschirm | PASS | Nach etwa 15 Sekunden blieb die Kameraverbindung sichtbar aktiv. Nach Rückkehr waren Raster und konservative Zusammenfassung vorhanden; der neue Clip blieb `lokal vollständig · Integrität geprüft; Quelle offen`. |
| 5 — kontrollierter Pocket-Power-Cycle | INCONCLUSIVE — Software-Folgefehler | Nach etwa 15 Sekunden ausgeschalteter Kamera stellte die App die Verbindung ohne Benutzereingriff automatisch wieder her und zeigte erneut das Raster. Die konservative Zusammenfassung mit drei früheren Prüfungen blieb korrekt; jedoch blieb der vorübergehende Hinweis „Automatische Sicherung wird vorbereitet“ nach 30–60 Sekunden sichtbar. Das ist kein sicherer laufender Transfer und keine zulässige Endanzeige. Hardwaretest pausiert, bis die Service/UI-Abschlussprojektion repariert und intern geprüft ist. |
| 6 — sichtbare Reason-/Datenschutzgrenze | PASS (sichtbar, Teilnachweis) | Der recoverable Verlust wurde ohne spekulative Fehlerursache oder sensible Diagnosewerte dargestellt. Die detaillierte app-private Export-/Share-Sheet-Grenze ist in dieser Sitzung nicht geöffnet worden und bleibt ein separater physischer Teilnachweis. |
| 9 — Capture-Day-Anzeige | INCONCLUSIVE | Die Raster-/Zusammenfassungsansicht lieferte in diesem Durchlauf keinen beobachtbaren Capture-Day-/Herkunftsbeleg. Es wurde keine Zeit-/Zeitzonenbehauptung aus dem Test abgeleitet. |

Der SSD-Zweig ist pausiert: Der sichtbare Vorbereitungsstatus muss nach dem Reparaturbuild bei
einem Plan ohne sichere neue Arbeit zuverlässig verschwinden oder in einen expliziten ehrlichen
Review-Zustand übergehen. Die detaillierte Export-/Share-Sheet-Grenze und Capture-Day-Quellenfakten
bleiben als `HARDWARE_DEFERRED` beziehungsweise `INCONCLUSIVE` erhalten.

## Nachtest unterbrochen — neuer Lifecycle-Befund

Nach dem In-Place-Update mit einem erzwungenen App-Neustart meldete die App bei eingeschalteter,
gespeicherter Kamera `USER_ACTION_REQUIRED` mit `CAMERA_UNAVAILABLE` nach nur einem Scan. Das
unterscheidet sich vom erwarteten begrenzten Launcher-Scanfenster und ist kein PASS. Die
Sitzungsdaten enthielten nur Zustand/Grund/Versuchszahl, keine Medien- oder Zugangsdaten.

Die plausible Softwareursache war ein verzögert zugestelltes, nicht epoch-gebundenes Service-STOP
aus dem vorigen Activity-Lebenszyklus. Die Reparatur bindet STOP an den auslösenden Session-Epoch;
ein STOP für einen abgelösten Epoch wird ignoriert und kann weder den neuen Host noch dessen
Backup-Runtime beenden. `DurableSessionRuntimeTest` deckt den abgelösten STOP explizit ab. Der
vollständige JVM-/APK-Checkpoint steht bei 525 Tests, null Fehlern und null Errors. Der nächste
Nachtest installiert in-place ohne Force-Stop und prüft zuerst genau die automatische Verbindung.

## Target confirmation after epoch-fence repair

With the Pocket briefly woken from standby, automatic connection and grid reconstruction succeeded.
The obsolete automatic-preparation progress bars were absent after the terminal no-work outcome.
This confirms the status-clearance repair in the observed path. The traveller reported that the
remaining diagnostic multi-field backup summary was not useful; its replacement is a separately
tested, single next-action sentence and requires its normal target display check before the SSD
branch resumes.

## Gebündelter Nachtest vorbereitet

Der Nachtest ist auf `e7a8ecb` mit APK-SHA-256
`A2082D6CC11909230FCBEE863E4E95651B9FE813002F74D2A70343F6CF02A728` vorbereitet.
Die interne Grundlage umfasst 526 JVM-Tests ohne Fehler. Er prüft in einer einzigen Sitzung:
automatische Verbindung ohne Rescan, verständliche Ruheanzeige, service-eigenen Fortschritt eines
neuen unkritischen Clips, Hintergrundverhalten, Power-Cycle-Recovery inklusive Epoch-STOP-Fence
und bereinigte Zustandsgründe. Die SSD-Schritte bleiben gemeinsam `HARDWARE_DEFERRED`, falls die
Topologie nicht zuverlässig bereitsteht. Der Ablauf steht in
`docs/hardware/FINAL_COMPLETE_APP_HARDWARE_TEST.md`; dieses Protokoll enthält noch keine Ergebnisse
dieses Nachtests.

## Nachtest Schritt 1/2 — neuer Befund

Die bekannte Kamera verband automatisch und das Raster erschien ohne Rescan (`PASS` für die
Verbindung). Nach etwa einer Minute zeigte die App jedoch weiterhin „Kameraliste wird geprüft“.
Das ist für eine bereits sichtbare, aber konservativ als unvollständig bewertete Inventur eine
irreführende Aktivitätsbehauptung und daher `FAIL` für die Ruheanzeige, nicht für die
Datensicherheitsentscheidung. Die Ursache war softwaretestbar: die Textprojektion verwendete
dieselbe aktive Formulierung sowohl für laufende als auch für terminal unvollständige Inventuren.
Die Reparatur zeigt im terminalen Fall „Kameraliste noch nicht vollständig“, ergänzt eine gezielte
Regression für genau diese Projektion und bestand den gezielten JVM-Test. APK-SHA-256 des
Reparaturbuilds: `928F860934F37F9E1A1B1BE1F3695984829E7C94DD6319EC78740844517AE2B9`.
Dieser neue Build ist vor weiteren Hardware-Schritten in-place zu installieren; keine Medien oder
App-Daten werden zurückgesetzt.

## Reparaturbuild — Zielgerät-Nachweis

Nach der In-place-Installation zeigte der App-Start bei bereits eingeschalteter Kamera zunächst
keine automatische Verbindung. Ein einmaliger Kamera-Power-Cycle stellte danach die Verbindung
automatisch wieder her und das Raster zeigte „Kameraliste noch nicht vollständig“. Damit ist die
Statusanzeige-Reparatur `PASS`; sie behauptet keine laufende Aktion mehr. Der Startpfad
„App-Prozess neu, bekannte Kamera bereits an“ bleibt ein separater `INCONCLUSIVE`/
Untersuchungsbefund: das Ergebnis ist nicht mit dem bereits beobachteten Power-Cycle-Recovery
gleichzusetzen. Keine manuelle Auswahl, kein Rescan, keine Löschung und keine Medienübertragung
wurden hierfür ausgelöst.

## Klarer Zustands- und Fortschrittsvertrag

Der Begriff „Kameraliste noch nicht vollständig“ wurde nach Nutzerfeedback präzisiert: Der
terminal-konservative Zustand lautet jetzt „Kamera-Sicherung wartet auf vollständige Dateiliste“.
Damit ist klar, dass keine neue automatische Übertragung oder Löschfreigabe erfolgt, ohne eine
nicht vorhandene Aktivität vorzutäuschen. Der Fortschrittsbereich erscheint nur für aktive
Planung/Übertragung: Er benennt Vorbereitung, Übertragung und anschließende Integritätsprüfung.
Eine Erfolgsmeldung bleibt kurz sichtbar und verschwindet dann; terminale Review-/Quellenwechsel
lassen keinen scheinbar laufenden Balken zurück. `BackupStatusCopyTest` (8) und
`AutomaticTransferUiStatePolicyTest` (3) sind grün. Reparaturcommit `ea091d2`, APK-SHA-256
`75AB42207F026D296707A8611A28744831FD931EC0B229424A3F9254F5B17ED4`.

Keine Löschung,
kein Reset und keine wiederholte Übertragung geschützter Medien wurden durchgeführt.

## Transport-/Statuskorrektur vorbereitet und installiert

Die S25-Beobachtung „acht neue Dateien“, danach unmittelbar HTTP 404 vor dem ersten Byte und keine
sichtbare Einzelaktivität, wurde als softwaretestbare Lücke klassifiziert. Die Korrektur auf
`eaf824c` wiederholt ausschließlich beobachtete, vorallokationsseitige 404/500-Antworten maximal
dreimal und zeigt währenddessen global sowie in exakt der betroffenen Videokachel den aktuellen
indeterminierten Wiederholungszustand. Erfolg geht in gemessenen Telefonfortschritt über;
erschöpfte/andere Antworten bleiben Review. Es gibt keine neue Dateiallokation während der
Wiederholung.

Vollständiger interner Checkpoint: **541 JVM-Tests, 0 failures, 0 errors**, Debug-APK SHA-256
`D788F45892CE67E3B7546E0EFA22D42852AC95F058D5DB3105507DD38310CDEE`.
Am 2026-09-26 wurde diese signer-kompatible APK mit `install -r` auf S25 `R3CYA022GAE` in-place
installiert und die App gestartet (PID 15225); vorhandene App-Daten wurden nicht gelöscht. Die
physische Retry-/Render-Zeit ist damit noch nicht als PASS behauptet und bleibt Schritt 3 des
gebündelten Hardwareplans.
