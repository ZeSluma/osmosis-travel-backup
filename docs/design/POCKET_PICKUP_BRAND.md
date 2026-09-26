# Pocket Pickup — Brand- und Oberflächenentscheidung

Status: **IMPLEMENTED_SOFTWARE_PROVEN** (2026-09-26). Dies ist eine reine, nicht-destruktive
Produktidentitätsänderung; Paketname, Signatur, Datenbank, Medienpfade und Backup-Regeln bleiben
unverändert.

## Festgelegte Identität

- **Name:** Pocket Pickup
- **Ton:** ruhig, sachlich, reisetauglich. Die Oberfläche informiert über einen sicheren lokalen
  Sicherungsvorgang, statt technische Aktivität dekorativ zu überbetonen.
- **Farbe:** helle, leicht kühle Neutralflächen; genau ein gedämpfter Petrolton für Aktionen und
  Fortschritt. Grün, Amber und Rot bleiben ausschließlich semantische Zustandsfarben.
- **Icon:** ein weißer, einfacher Taschenausschnitt mit eingehendem Pfeil auf Petrol. Das Zeichen
  bedeutet "Medien sicher übernehmen" ohne Kamera-/Cloud- oder Löschversprechen.
- **Layout:** Das vorhandene klare Raster und die Status-/Fortschrittszone bleiben. Sie waren
  bereits auf Sicherheits- und Zustandsverständlichkeit ausgerichtet; keine neue dekorative
  Oberfläche verdrängt den aktuellen Transferzustand.

## Umsetzung und Grenzen

- `app_name` und die sichtbare Benachrichtigungsbezeichnung verwenden Pocket Pickup.
- Das adaptive Launcher-Icon wird auf Android 10+ (minSdk 29) aus Vektoren erzeugt und skaliert
  ohne gerätespezifische Rastervarianten.
- Die UI-Palette wurde in Tag- und Nachtmodus synchron angepasst. Material You darf auf Android 12+
  weiterhin die Systemfarben einfließen lassen; Lesbarkeit und semantische Farben bleiben davon
  getrennt.
- Keine Datenmigration, App-Neuinstallation, Medienverschiebung, Cloud-Anbindung oder
  Kameraoperation ist Teil dieser Änderung.

## Abnahme

Der Debug-Build muss Ressourcenverlinkung und Manifest-Label kompiliert nachweisen. Die
visuelle Abnahme erfolgt als Teil des einen gebündelten S25-Hardwaretests: Launcher-Name/-Icon,
Tag-/Nacht-Kontrast sowie aktive und terminale Fortschrittsanzeige werden ohne erneute
Übertragungs- oder Löschaktion geprüft.
