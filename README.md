# swiftp-aar-core

A headless, GUI-stripped AAR build of [SwiFTP](https://github.com/ppareit/swiftp)'s core FTP server engine for Android, packaged as a standalone library for use as a dependency in other apps.

## What this is

SwiFTP is a full Android FTP server app, GUI included. This repository takes just its core FTP-serving logic — the parts that run the actual server, handle FTP commands, and manage storage access — and packages it as an `.aar` library with no `gui` package, no activities, no fragments, and no Settings UI. It's meant to be embedded inside a host app that provides its own UI, notification styling, and settings screens on top of it.

This is a personal fork maintained for use in my own Android projects. It is not affiliated with or endorsed by the upstream SwiFTP project.

## Features

- Full FTP server engine — control connection, file listing, upload/download, directory operations — extracted from [SwiFTP](https://github.com/ppareit/swiftp), with no GUI dependencies
- Runs as a standard Android foreground `Service`, controllable via broadcast actions (`ACTION_REQUEST_START`, `ACTION_REQUEST_STOP`) and broadcasts its own state (`ACTION_STARTED`, `ACTION_STOPPED`, `ACTION_FAILEDTOSTART`)
- Scoped Storage / SAF support via `AllowedFolders`, allowing one or more granted folder trees to be served without legacy broad storage permissions
- Zero UI code or resources beyond the bare minimum (strings, a launcher-independent theme) — designed to be embedded in a host app that supplies its own settings screen, notification styling, and folder-picker UI
- Kotlin + Java interop supported in the build

## Known limitations

- **FTPS/TLS is not functional.** This is an upstream limitation, not specific to this fork — it's reproducible on the official, unmodified SwiFTP app as well. TLS handshakes fail; see [upstream issue #261](https://github.com/ppareit/swiftp/issues/261) for the request to allow disabling TLS entirely. Plain FTP works normally. If you need encrypted transport, consider tunneling through a VPN (e.g. Tailscale/WireGuard) instead.
- Certificate generation for FTPS was never implemented upstream — FTPS is designed around the user manually importing their own `.jks`/`.bks` certificate files, which requires UI this library intentionally does not provide.
- This library does not include any settings persistence UI, notification icon, or folder-picker UI. The host app is expected to provide all of these.

## Requirements

- `minSdk` 23
- JDK 17
- Android Gradle Plugin with Kotlin Android plugin support (this module builds mixed Java/Kotlin sources)

## Building

Clone the repository and build the release AAR directly:

```bash
git clone https://github.com/thechaosman0225/swiftp-aar-core.git
cd swiftp-aar-core
./gradlew :swiftp-core:assembleRelease
```

The built artifact will be at:

```
swiftp-core/build/outputs/aar/swiftp-core-release.aar
```

There is no Maven/Maven Local publishing configured — this is distributed as a raw `.aar` file. See [Consuming this library](#consuming-this-library) below for what that means for your dependency setup.

### Repository layout

- `swiftp-core/` — the library module itself; this is the only module that gets built into the AAR
- `.github/workflows/` — CI workflows:
  - **Build SwiFTP Core AAR** — builds the release AAR and uploads it as a workflow artifact
  - **Clean Build (No Cache)** — a from-scratch build with no Gradle cache, run manually to catch cache-masked or environment-specific issues before they surface elsewhere

## Consuming this library

Because this is a bare `.aar` file rather than something published to a Maven repository, Gradle has no dependency metadata to resolve automatically — you need to declare `swiftp-core`'s own dependencies directly in your app module as well, or you'll hit runtime `ClassNotFoundException`/`NoClassDefFoundError` errors for classes that are actually just missing from your final APK's dex.

1. Copy the built `.aar` into your app module's `libs/` folder.
2. Add it as a file dependency, along with the transitive dependencies it needs:

```kotlin
dependencies {
    implementation(files("libs/swiftp-core-release.aar"))

    implementation("net.vrallev.android:cat:1.0.5")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("org.jetbrains:annotations:23.0.0")
}
```

Check `swiftp-core/build.gradle` for the current, authoritative dependency list — the block above may drift out of sync with it over time.

3. Your app's `AndroidManifest.xml` must declare a custom `Application` class extending `be.ppareit.swiftp.App` (or use it directly), or the library's internal state (`App.getAppContext()`) will be `null` and cause crashes:

```xml
<application
    android:name="be.ppareit.swiftp.App"
    ...>
```

4. Add the required permissions if your app's manifest doesn't already declare them — check `swiftp-core/src/main/AndroidManifest.xml` for the current list (network state, wifi state, wake lock, foreground service, notifications).

## Credits and attribution

This project is a derivative work based on [SwiFTP](https://github.com/ppareit/swiftp), originally created by **David Revell** and currently maintained by **Pieter Pareit**. All core FTP protocol handling, server logic, and the original architecture of this codebase are their work — this repository's contribution is limited to extracting that logic into a GUI-free, standalone library form, along with the fixes and adaptations documented in this repository's commit history.

Please direct any appreciation for the underlying FTP server implementation to the upstream project. Bug reports specific to the *extraction/packaging* of this AAR are welcome here; bugs in the underlying FTP protocol handling itself are best reported upstream.

## License

This project is licensed under the **GNU General Public License v3.0 or later (GPL-3.0-or-later)**, the same license as upstream SwiFTP. See [`COPYING`](./COPYING) for the full license text.
