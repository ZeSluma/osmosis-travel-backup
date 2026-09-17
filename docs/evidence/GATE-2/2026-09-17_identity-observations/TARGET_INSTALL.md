# Schema5 target installation checkpoint

2026-09-17, user explicitly resumed. Installed code commit dd73697819032a28f240155c516a26f7e460cc98. Target model SM-S938B. No serial recorded.

Before installation, adb -d get-state returned device. Read-only hardwareAudit projection confirmed schema4,1 source,7 snapshots,4 assets/recordings/members/replicas. Latest snapshot fc1c488f4b5338481c1b4d4dd15eb5d2b0405ea10cdf872d2b873ee79a19f12a retained3 members and INCOMPLETE. Original38MB row retained one candidate, selected locator absent/AMBIGUOUS; large file retained its selected locator/PRESENT_UNVERIFIED; third103945077-byte candidate retained DJI_FILENAME day2026-09-17. Weak null-size history retained. No new observations/merges inferred.

Metadata-only `adb -d shell stat -c '%s'` of the two protected Movies/Osmosis original paths returned38447651 and3071380142. No media content read/hash/copy.

Rechecked SHA256 before install:
- app-debug.apk:0EB27248ED4BA5620753413B3BF2C7616F1CD86C89883440C5C4C5F5A7EAC561
- app-debug-androidTest.apk:F3B05A9676B0172439F2992DE527D4C65DDB93D2F178522B26C93EE48AC7DEBB

`adb -d install -r <verified-debug-apk>` and `adb -d install -r <verified-test-apk>` each returned Success. No uninstall/data clear. `adb -d shell am start -W -n dev.konraditurbe.osmosis/.ui.MainActivity` returned Status:ok, COLD,249ms. Combined install/launch command exit0,6.23s. No synthetic instrumentation suite executed on target.

Migration/reselection NOT_TESTED at this checkpoint: Room opens lazily when normal enumeration is processed. Need one ordinary camera search/connect on this newly installed build; no media selection/download. This is new-schema integration, not an attempt to establish source identity by repeating scans. After user confirmation, inspect sanitized persisted schema/history/observations/candidate references/timestamps and protected file sizes. Do not infer migration PASS from installation or launch.

G2 remains BLOCKED pending that integration. No media modified; no full-snapshot trust/UTC/integrity claimed. Earlier ADB-absent checkpoint is historical and superseded for availability only.
