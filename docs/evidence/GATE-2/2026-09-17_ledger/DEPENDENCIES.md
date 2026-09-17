# GATE-2 dependency review

2026-09-17: official AndroidX release page identifies Room2.8.5 stable (September09). Runtime/compiler POMs fetched from Google Maven confirm version and Apache2 license. Room is the approved typed transactional persistence choice. KSP2.3.12 is Google's latest stable release; Apache2 license confirmed via repository license API. It requires AGP>=8.12; project uses8.13.2. KSP2 avoids KAPT and its build-cache metadata path. Kotlin remains2.4.20. Room Gradle plugin configures schema task inputs/outputs; no new repository or mutable version introduced.

Direct OSV queries for androidx.room:room-runtime:2.8.5, room-compiler:2.8.5 and com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.12 returned {} (no returned advisory). This is limited coordinate/time evidence, not proof of zero vulnerabilities; actual transitive resolution and processor compatibility remain to be tested.

Sources: https://developer.android.com/jetpack/androidx/releases/room ; https://github.com/google/ksp/releases/tag/2.3.12 ; https://dl.google.com/dl/android/maven2/androidx/room/room-runtime/2.8.5/room-runtime-2.8.5.pom ; https://api.osv.dev/v1/query . No external instructions executed. Initial POM parser attempted XML conversion of PowerShell byte array and failed; corrected by explicit UTF8 decoding before review, no dependency decision based on failed parse.
