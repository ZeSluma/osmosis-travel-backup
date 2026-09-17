# Official-source research index

Access date 2026-09-17. These are primary platform references, not evidence that the unchanged Osmosis build implements the proposed behavior. Design decisions and tradeoffs are in ADR0003/0004; runtime assumptions remain HIL obligations. No secondary search result is used as authority. Android documentation currently describes Android17/target37 enforcement; treat it as future app migration, not an unreleased-platform assumption.

| Topic | Primary reference | Where applied |
|---|---|---|
| Transfer API selection | https://developer.android.com/develop/background-work/background-tasks/data-transfer-options | ADR0003 |
| UIDT | https://developer.android.com/develop/background-work/background-tasks/uidt | ADR0003 |
| FGS types | https://developer.android.com/develop/background-work/services/fgs/service-types | ADR0003 |
| Launch and background restrictions | https://developer.android.com/develop/background-work/services/fgs/launch and https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start | ADR0003 |
| FGS timeouts | https://developer.android.com/develop/background-work/services/fgs/timeout | ADR0003 |
| Android16 quota changes | https://developer.android.com/about/versions/16/behavior-changes-all | ADR0003 |
| FGS user stop | https://developer.android.com/develop/background-work/services/fgs/handle-user-stopping | ADR0003 |
| Force-stopped state | https://developer.android.com/about/versions/15/behavior-changes-all#stopped-state | ADR0003 |
| CPU awake choice | https://developer.android.com/develop/background-work/background-tasks/awake | ADR0003 |
| Companion association/presence | https://developer.android.com/develop/connectivity/bluetooth/companion-device-pairing | ADR0003 |
| Network request/approval | https://developer.android.com/develop/connectivity/wifi/wifi-bootstrap | ADR0004 |
| Network-bound IO | https://developer.android.com/reference/android/net/Network | ADR0004 |
| Callback lifecycle | https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback | ADR0004 |
| Local-only concurrency | https://source.android.com/docs/core/connect/wifi-sta-sta-concurrency and https://developer.android.com/reference/android/net/wifi/WifiNetworkSpecifier | ADR0004 |
| Local-network permission | https://developer.android.com/privacy-and-security/local-network-permission | ADR0004 / N05 |
| Room/migrations | https://developer.android.com/training/data-storage/room and https://developer.android.com/training/data-storage/room/migrating-db-versions | LEDGER_AND_INTEGRITY |
| MediaStore | https://developer.android.com/training/data-storage/shared/media | Pending/publication design; cross-system transaction strategy is our design |

Source inspection anchors at exact baseline commit: MainActivity.onDownloadClicked/startCameraScan/loadMorePages; ApJoiner.join/rejoin/release; HttpClient.download; DumlTransport socket construction; DumlSession TCP construction; CameraSession.queryNewestPage/fetchNextPage/collectStores; CameraFile identity/type/sidecar/group fields; MediaDownloader.downloadOne/downloadedUri; MediaPreviewActivity companion/group discovery. These support observed design gaps, not unmeasured protocol stability claims.

Conflicts/limits: no runtime S25 concurrency capability measurement; no confirmed Pocket checksum/stable storage generation; no exhaustive Pocket originals inventory; no system sleep/active-transfer measurement; no approved SDK/dependency upgrade. Resolve by defined tests, not documentation assumptions.
