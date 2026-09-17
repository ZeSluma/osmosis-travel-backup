# Intent-to-system test matrix

Status: planned, NOT_TESTED unless linked baseline evidence explicitly says otherwise. No new tests/hardware actions executed for the rebase. Each run records app/source commit, device/OS/firmware, policy/schema version, exact fault point, expected/actual durable state, safe numeric events and test outcome. Fakes test invariants; hardware proves platform/protocol claims. Destructive storage/source scenarios require synthetic data or separately authorized disposable media, never current originals.

| ID | Requirement / proposed gate | Experiment | Acceptance oracle |
|---|---|---|---|
| X01 | R16-17 / G2,G7 | Repeated Open/Start, rotate/recreate, concurrent notification action | One fenced coordinator and at most one datalink/asset writer; identical ledger projection |
| X02 | R17,30 / G7 | S25: start multi-GB batch then app-switch, lock/screen off; stock power settings | Transfer continues or truthful platform stop/action; no Activity dependency, false completion or missing notification; record rate/CPU/RSS/battery/thermal |
| X03 | R17,24 / G2,G3,G7 | Kill process at every boundary listed below, including no callback | Recover actual state, no false VERIFIED, no duplicate IO; permitted restart timing documented |
| X04 | R17,29 / G7 | Distinguish recents removal, OS kill, Task Manager Stop, Settings force-stop, reboot | No evasion of user stop; explicit reopening when required; preserved ledger/files |
| X05 | R17,30 / G7 | FGS visible-start race, notification denied, background-start rejection, explicit Pause/Cancel | Type/permissions checked, user-visible truthful status, durable intent before resource cleanup |
| X06 | R32 / G1,G7 | Android16 job quota/thermal/stop reasons for any proposed WorkManager/UIDT experiment | Bounded work obeys platform eligibility; quota does not masquerade as credential failure |
| X07 | R33 / G7 | Long multi-file/multi-hour soak, thermal and battery constrained runs | Bounded memory/concurrency/CPU and no indefinite scan/wake lock; budgets defined from measurements before release |
| N01 | R18,27 / G7 | Temporary Wi-Fi off/AP re-establishment, camera available, foreground app | Automatic bounded reconnection then separate automatic safe partial continuation; no routine rescan/retry |
| N02 | R28,36 / G7 | Distinct BLE loss, awake foreground protocol drop, Android background loss | Separate observed reason/confidence; no false stale-password or sleep diagnosis |
| N03 | R27 / G1,G7 | Specific Network request approved/rejected/forgotten; epoch replaced mid-IO | Correct approval state; stale callbacks/old sockets cannot own new session |
| N04 | R27 / G1,G6 | Query local-only concurrency capability on SM-S938B; camera plus controlled Internet traffic with mobile on/off | Record actual support/routes; cloud never uses camera AP; sequential fallback works with no Internet |
| N05 | R32 / G1,G7 | Android16 local-network restriction simulation; Android17 target37 grant/deny/revoke | TCP/UDP permission failures separate from camera failure; no network API bypass assumption |
| C01 | R28,36 / G7 | Independently idle playback, AP timeout, display-off, standby, power-off, wake, BLE availability | Time-correlated camera and app states; camera sleep remains distinct from background; no setting changes to hide cause |
| C02 | R36 / G7 | Safe new active-transfer observation versus matched idle duration | Determine whether transfer inhibits sleep/AP loss; if untested keep UNKNOWN, never infer from display alone |
| I01 | R19 / G2,G3 | Reconnect/reboot/volume change/filename reuse/rollover/source replacement | Strong identity match or explicit uncertainty; changed asset never appended/skipped by name |
| I02 | R20-21 / G4 | Every actual Pocket media mode, internal+card, >1 page, RAW/audio/metadata/group members | Independently accounted required inventory; no required companion omitted; unknown types block completeness |
| I03 | R21 / G4 | Empty page, delayed mount, truncated/repeated page, page-N failure | Enumeration incomplete rather than false empty/complete; bounded retries |
| I04 | R21 / G4 | New source, active recording, mode change, camera deletion during sync | Generation invalidated/extended; finalized-object rule; no local cascading delete |
| T01 | R22-24 / G3 | Disconnect at 0/1/chunk-boundary/mid/final byte; delayed/stale response | Correct partial checkpoint and exact ranged continuation with no byte duplication/gap |
| T02 | R22 / G3 | HTTP200 on range, malformed/mismatched Content-Range, 416, changed total/identity | Never append invalid response; isolate restart staging; no corrupted complete state |
| T03 | R22-23 / G3 | 404/500 busy, zero-byte body, premature EOF, extra bytes, close/flush failure | Bounded busy retry; size/integrity failure remains not verified |
| T04 | R23 / G3 | Source checksum available/unavailable, local-only hash, reread failure | Assurance accurately recorded; local hash never claimed as remote equality |
| S01 | R25 / G3 | Preflight low space, ENOSPC midstream, insert succeeds/write fails | Retained truthful partial; verified file never overwritten; specific storage action |
| S02 | R24-25 / G2,G3 | Orphan pending URI, absent file, duplicate name, ledger/disk offsets disagree | Quarantine/reconcile safely, no adopt-by-name or blanket cleanup |
| S03 | R3,24 / G2 | Room migrations every supported version, upgrade interrupted, unknown downgrade | No destructive fallback; all verified identities/replicas preserved or explicit blocked state |
| R01 | R4,16,18 / G4 | Reopen after complete, repeated Start, reconnect with incomplete queue | Ledger recognition and automatic planning; verified files not retransferred, no manual queue dependence |
| R02 | R26,31 / G5,G6 | SSD unplug/remount/grant revoke; cloud offline/token expiry/conflict | Phone verification independent; required redundancy incomplete, no false safe-clear |
| P01 | R13,28,34 / G1+all | Inject secrets/GPS/filenames/raw frames/exception URLs in every failure branch | Export/log allowlist contains none; reason correctness, bounded retention, lockscreen privacy |
| D01 | R35 / G1+future CI | Clean Windows autocrlf variants and Linux golden resources, CI matrix | Canonical bytes or robust test semantics, original failure retained; no functional fixes bundled with rebase |

Crash injection points for X03: before enumeration; each page commit; before URI creation; after URI creation/before URI journal; before transfer; during write/before flush; after flushed bytes/before checkpoint; after checkpoint/before next read; during verification; after verification intent/before MediaStore finalization; after finalization/before LOCAL_VERIFIED commit; between files; during retry/reconnect; after owner lease replaced. Assert complete asset implies all required recording members and proof, not UI success. Fakes expose injectable clock/backoff, network, camera repository, stream, writer, verifier and database transaction boundaries; model-based transition tests reject illegal promotions.

## Capture-day organization — R-037 (all NOT_TESTED)

Run resolver/fault tests with synthetic dates first. Future HIL uses S25 Ultra + Pocket 4P and separately authorized non-critical recordings only, after explicit hardware resume. No existing original/local file is moved, edited, deleted or redownloaded. Do not change phone/camera clocks or zones without a bounded test window and restoration plan; baseline connection itself attempts camera clock synchronization. Evidence contains synthetic dates or approved test-only timestamp fields, opaque asset IDs and sanitized numeric/path assertions, never raw metadata dumps/GPS/content.

| ID | Gate / experiment | Acceptance oracle |
|---|---|---|
| CD01 | G2/G4; two recordings from one capture day, then repeated sync/concurrent Start | One automatically created day folder, one replica per original, no duplicate folder or provider-generated duplicate filename; no manual folder creation |
| CD02 | G2/G4; recordings from two independently established capture days | Exactly two canonical YYYY-MM-DD folders; each asset in its original day; valid calendar-year formatting across New Year |
| CD03 | G1/G4; postpone sync several days and change phone travel zone before sync | Verified capture day wins over download day/current zone; source/offset provenance persisted; no claim filename trustworthy without independent model/firmware evidence |
| CD04 | G2/G3/G7; interrupt then restart/resume across midnight/phone-zone change, crash around directory/path reservation and URI journal | Same frozen relative path and actual partial URI retained; bytes safely continue, one folder/file, no duplicate or unnecessary retransfer |
| CD05 | G1/G4; primary plus available DNG/audio/metadata, differing sidecar mtime and late discovery | Parent capture-day grouping retained; all required companions co-located, mixed-MIME adapter works; missing required member still incomplete; unavailable target types remain NOT_TESTED with fake coverage separately |
| CD06 | G1/G4; independently record just before/after local midnight, recording spanning midnight; simulated/authorized DST overlap/gap and date-line travel | Independent recordings use their own capture-local days; split parent stays together; no conversion by current phone zone, no silent normalization; actual Pocket offset/zone evidence recorded separately from synthetic tests |
| CD07 | G1/G4; missing zone, instant-only source, invalid/unverified filename, wrong clock, metadata/remote disagreement | Exact documented fallback and provenance/conflict warning; nullable instant when local-only; no invented authoritative date, source priority respected; never rewrite original metadata |
| CD08 | G5/G6; replicate same group days later to different roots; name collision and lost permission/retry | Same ledger day/member mapping under each root, stable non-overwriting collision name, no upload-date folders; required replica state independent |

Pocket investigation protocol: compare known test recording start with allowlisted capture-field values, remote-file timestamp and filename; determine whether offset/zone exists and what each value means. Record camera clock/zone before any app connection and again after baseline syncTime attempt, with no assumption its command succeeded. Compare recordings before/after independently authorized travel-zone/clock transitions; preserve old recordings unchanged. If exact edge cases cannot safely be reproduced on hardware, report NOT_TESTED and retain synthetic coverage separately. Confirm S25 storage behavior for all required asset types before R-037 acceptance.

## Planned Android16 local-network simulation (not executed)

Use a separately approved test window, record initial settings and app permissions, and keep real originals safe. Under [official instructions](https://developer.android.com/privacy-and-security/local-network-permission):

```text
adb shell am compat enable RESTRICT_LOCAL_NETWORK dev.konraditurbe.osmosis
adb reboot
# Test TCP/UDP denied and NEARBY_WIFI_DEVICES granted; record actual OS support.
adb shell am compat disable RESTRICT_LOCAL_NETWORK dev.konraditurbe.osmosis
# Restore recorded permissions/settings and verify ordinary behavior.
```

Do not flash the user's phone or run these during the hardware pause. Unsupported compat behavior is BLOCKED/NOT_TESTED with exact build info, not permission to loosen production security. On a future target37 build test ACCESS_LOCAL_NETWORK separately; no current manifest change.

## Existing evidence carried forward, not substitute for these tests

GATE-0: manual range continuation 374,356,968 -> 3,071,380,142 bytes PASS; automatic reconnect/resume FAIL; file/MediaStore persistence PASS; post-restart UI recognition FAIL; awake foreground drop observed with root cause unconfirmed. Screen-off/service recovery, strong identity, complete original inventory, sleep causes and concurrent Internet are NOT_TESTED. Historical controlled force-stop tested completed-file persistence only, not recovery of an active backup process. No automatic claim is promoted from that test.
