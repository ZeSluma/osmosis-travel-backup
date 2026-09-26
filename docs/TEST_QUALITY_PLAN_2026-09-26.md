# Prüf- und Qualitätsplan — Pocket Pickup

Stand: 2026-09-26. Dieser Plan ersetzt nicht die Sicherheitsinvarianten oder den gebündelten
Hardwareplan. Er ist die zwingende Softwareprüfung vor einem weiteren Hardwaredurchlauf.

## Auslöser und Lessons Learned

Die Rückmeldungen aus den bisherigen S25/Pocket-Sitzungen ergeben sechs wiederkehrende Klassen.
Sie wurden zuvor zu stark als einzelne Text-, Build- oder Hardwarebefunde behandelt.

| Nutzerbefund | Warum die frühere Prüfung ihn nicht sicher fand | Dauerhafte Gegenmaßnahme |
|---|---|---|
| Gespeicherte Kamera verbindet sich nach Power-Cycle/Rescan/Start unterschiedlich. | Zustandsmaschine und einzelne Recovery-Pfade waren getestet, nicht die komplette Abfolge mit alter Callback-/Sitzungsablösung. | Szenario-Matrix: Start mit Kamera aus/an, Rescan, Hintergrund, Power-Cycle, Ersatzepoch; jeder Pfad prüft genau eine aktive Sitzung und Revalidierung vor Datenverkehr. |
| „Wird geprüft“ blieb stehen, obwohl kein Worker aktiv war. | Text-Copy wurde separat getestet, aber nicht gegen die tatsächliche Planaktion abgeglichen. | Jede sichtbare Formulierung erhält einen Zustandsvertrag: aktiv nur bei einem Writer, offen bei fehlendem Beleg, terminal bei abgeschlossenem Ergebnis. |
| Neue automatische Transfers bekamen sofort 404 und wurden als endgültig angezeigt. | Der manuelle Client kannte den transienten Busy-Fall, der strenge automatische Pfad hatte keinen identischen Transport-Fake. | Vorallokations-Transportmatrix: 404/500→Erfolg, begrenzt erschöpft→Review, nicht erlaubter Fehler→kein Retry, Abbruch→kein Retry. |
| Gesamt- und Videoanzeige zeigten nicht immer denselben aktuellen Zustand. | Dauerhafte Badge-Logik und temporäre Writer-Projektion waren getrennt, ohne vollständige Übergangstabelle. | Global-/Pro-Datei-Matrix mit Vorbereitung, Retry, Prozentfortschritt, Integritätsabschluss, Review, Terminal-Reread und stale Callback. |
| Statuspublishes ließen Grid und Filterleiste hochfrequent flackern. | Es gab keinen Idempotenz-/Renderbudget-Test; jedes Publish löste ein vollständiges RecyclerView-Rebind aus. | Zustands-Diff ist Pflicht: unverändertes Publish = null Rebind; Fortschritt = nur aktive Kachel; Terminal = nur frühere aktive Kachel. |
| Text war technisch korrekt, aber für die Nutzung nicht verständlich. | Copy-Prüfung bewertete zu wenig den aktuellen Nutzerentscheid und die visuelle Hierarchie. | Jede Karte hat eine Hauptaussage zum aktuellen Sicherungsstand, einen optionalen Grund und nie mehr als eine aktive Aktion; Screenshot-/Zielgerät-Review ergänzt die Logiktests. |
| Der offene Ruhe-/Prüfstatus beantwortete nicht „läuft etwas / ist das Telefon gesichert / was fehlt?“. | Die Prüfung bewertete Fail-Closed-Copy, aber nicht die vollständige Nutzerentscheidung in einem terminalen Nicht-Aktiv-Zustand. | Für jeden Ruhe-/Offenstatus drei explizite Assertions: Aktivität ja/nein, Telefonkopie ja/nein, Gesamtsicherung/SSD ja/nein; kein Wort darf eine nicht gestartete Prüfung suggerieren. |

## Übernommene Prüfgrundsätze

- Android empfiehlt einen Mix aus kleinen lokalen Tests und größeren Integrations-/UI-Tests statt
  nur einer Testart. UI-Tests sollen reale Interaktionen und sichtbare Reaktionen prüfen.
  [Android testing fundamentals](https://developer.android.com/training/testing/fundamentals)
  und [Automated UI tests](https://developer.android.com/training/testing/ui-tests).
- Lifecycle-Ereignisse müssen gezielt simuliert werden, nicht als indirekter Nebeneffekt eines
  manuellen Tests. [Activity lifecycle testing](https://developer.android.com/guide/components/activities/testing).
- RecyclerView-Updates müssen minimal sein. Android empfiehlt differenzbasierte Updates, damit
  unnötige Bindings vermieden werden und Änderungen verständlich animiert bleiben.
  [RecyclerView/DiffUtil](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView).
- Sichtbare Zustände werden als immutable, beobachtbare Zustandswerte behandelt; wiederholte
  identische Publikationen dürfen die UI nicht verändern. [Android UI layer](https://developer.android.com/topic/architecture/views/ui-layer).

## Verbindliche Software-Testmatrix

| Schicht | Muss prüfen | Nachweis |
|---|---|---|
| Unit: Transfer | Quellantwort vor Byte-Write, Retry-Bounds, Cancellation, nicht erlaubte Antwort, keine Allokation vor erfolgreicher Quelle. | `CameraTransferSourceTest`, `SingleAssetTransfer`-Reihenfolgen-Audit. |
| Unit: Status | Jede globale und per-Datei-Phase, Prozentgrenzen, unverändertes Publish, terminales Overlay-Entfernen. | `AutomaticTransfer*PolicyTest`, `LiveTransferFileProjectionPolicyTest`, `BackupProjectionDiffPolicyTest`. |
| Integration: Sicherheit | Vollständiges Inventar, eindeutige Quelle, ein Writer, keine doppelte Allokation, Review bleibt Review, SSD bleibt unabhängig. | Ledger-/Runtime-/Replica-Szenariotests. |
| Integration: Lifecycle | Hintergrund/Vordergrund, Prozess-/Epochwechsel, stale Callback, neu registrierter Beobachter. | Liveness-/Session-/Notifier-Szenariotests. |
| UI: Rendervertrag | Unveränderte Projektion bindet keine Zelle; Bytefortschritt bindet nur die aktive Kachel; Retry ist indeterminiert; terminale Daueranzeige kommt aus dem Ledger. | Policy-Test plus Android-UI-/Screenshot-Test, sobald die Fake-Projektion in die UI injizierbar ist. |
| UI: Ruhe-/Offenvertrag | Karte beantwortet Aktivität, Telefonkopie und Gesamtsicherung/SSD getrennt; fehlender Beleg ist ein Grund, keine erfundene Arbeit. | Neue Copy-/Präsentationsmatrix für lokale Kopie ohne Integritätsbeleg. |
| Vollcheckpoint | Vollständige JVM-Suite, Debug-APK, Prüfsumme, Diff-/Invariantenreview. | Protokollierte Testsumme/Hash; keine Hardwarebehauptung. |

## Vor Hardware zwingend

1. Neue oder geänderte sichtbare Zustände müssen in der Matrix alle fünf Ausgänge haben: aktiv,
   Fortschritt, Erfolg, fail-closed, stale/ersetzt.
2. Ein reproduzierbarer Fehler muss mindestens einen Test auf derselben Entscheidungsgrenze haben,
   nicht nur einen ähnlichen Copy-Test.
3. Ein Publish-/Observer-Pfad muss idempotent und auf sein Renderbudget geprüft sein.
4. Erst danach werden alle physisch offenen Punkte in **einem** nicht-destruktiven Ablauf getestet.

## Lessons Learned und verschärfter Prüfstandard (2026-09-27)

**Ergebnis:** Die bisherigen Prüfmethoden waren für fachliche Sicherheitsregeln wirksam, aber
nicht vollständig wirksam für sichtbare, zeitkritische Android-UI-Übergänge. Der Fehler war nicht
eine fehlende Unit-Testmenge, sondern eine Lücke zwischen einer korrekten Zustandsfunktion und
einem echten gerenderten Frame auf dem S25. Die Rückmeldungen zum Flackern und zur veralteten
Gesamtzeile hätten durch einen strengeren Render-/Freshness-Vertrag früher auffallen müssen.

Ab jetzt gilt zusätzlich:

1. **Einheitliche Quelle:** Jede globale Aussage über eine Datei muss aus derselben langlebigen
   `BackupDisplayPolicy` stammen wie die zugehörige Kachel. Ein Planaktionszähler ist niemals
   allein eine nutzerseitige Zustandsbehauptung.
2. **Schnellfolge-Simulation:** Für jedes live aktualisierte Element werden mindestens
   `0 % → Zwischenwert → Integritätsabschluss → langlebiger Terminalzustand` und die Entfernung
   des Overlays simuliert. Jeder Schritt nach dem ersten muss den dedizierten Payloadpfad nutzen;
   kein Schritt darf zu einem vollständigen Grid-Bind zurückfallen.
3. **Frische statt Textvergleich:** Jeder sichtbare Zustand wird mit aktivem Worker,
   letzter langlebiger Evidenz und erwarteter nächster Aktualisierung geprüft. Ein Text ist nur
   PASS, wenn diese drei Fakten zugleich zutreffen.
4. **Geräte-Rendervertrag:** Vor einem Hardwaretest enthält der Plan eine explizite Sichtprüfung
   für Rasterstabilität, Fortschrittsbewegung, terminales Ausblenden und Karten-/Kachel-Konsistenz.
   Diese Prüfung ist nicht durch JVM- oder Screenshot-Behauptungen ersetzbar; ihr Ergebnis bleibt
   `HARDWARE_PENDING`, bis sie auf dem Zielgerät beobachtet wurde.
5. **Fehler-Rückkopplung:** Jeder von der Hardware oder dem Nutzer gefundene sichtbare Fehler
   eröffnet zuerst eine Reproduktion an genau derselben Entscheidungsgrenze, dann eine Regression
   und erst danach wieder eine gebündelte physische Prüfung.

Die neue Regression `rapidLiveSequenceNeverFallsBackToAFullGridBind` implementiert Punkt 2. Die
vollständige Matrix bleibt dabei konservativ: ein extrem kurzer echter Transfer darf direkt zum
Terminalzustand gehen, aber nie einen falschen Fortschritt erfinden oder eine alte aktive Anzeige
zurücklassen.

## Aktuelle Abarbeitung

Die neue Diff-Policy beseitigt den dokumentierten Vollgrid-Rebind. Danach folgt ein Volltest,
eine erneute Abdeckungsliste gegen diese Matrix, Build/Hash und erst dann die Aktualisierung des
gebündelten Hardwaretests. Physische Kamera-/SSD-Ergebnisse bleiben bis zu ihrer Beobachtung
unbestätigt.

## Ergebnis des vollständigen Software-Audits

Am 2026-09-26 wurde die gesamte JVM-Suite nach Umsetzung der Diff-Policy ausgeführt:

```text
:app:testDebugUnitTest :app:assembleDebug --no-daemon
547 tests, 0 failures, 0 errors, BUILD SUCCESSFUL
```

Debug-APK SHA-256:

```text
C39082CE3674DEB70548BB7C8D22F23E1080C6454187EFBF7EAC57B50BE43B08
```

Die sechs in der Lessons-Learned-Tabelle genannten Fehlerklassen haben jetzt jeweils mindestens
eine direkte Entscheidungs- oder Zustandsprüfung. Nicht durch Software beweisbar bleiben nur die
eindeutig benannten physischen Kriterien: reales Pocket-Verhalten bei Verbindung, Busy-Response
und Render-Timing sowie USB-SSD-Topologie. Sie bleiben in einem gebündelten, nicht-destruktiven
Hardwareablauf; keine davon wird als softwareseitig PASS ausgegeben.

Der letzte offene Ruhe-/Prüfzustand ist zusätzlich umgesetzt: `BackupStatusCopyTest` prüft jetzt
die vollständige dreiteilige Nutzerentscheidung „keine Aktivität / Telefonkopien ungeprüft /
Gesamt- und SSD-Sicherung unbestätigt“. Der Build dieses Abschlusses hat die oben dokumentierte
Prüfsumme. Seine Zielgerät-Darstellung ist bewusst für den nächsten gebündelten Hardwaretest
reserviert.
