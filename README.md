# rust-gradle-plugin (RGP) [WIP]

A Gradle plugin for building Rust projects via Cargo targeting Desktop, Android, and iOS.

## Usage

`settings.gradle.kts`:

```kotlin
plugins {
  id("dev.rushii.rust-gradle-plugin") version "1.0.0"
}
```

`build.gradle.kts`:

```kotlin
rust {
  create("libhello") {
    projectPath.set("./src/main/rust") // Can be any path
    libName.set("hello") // The `lib.name` property from Cargo.toml
    targets.addAll("x86_64-pc-windows-msvc") // Any installed rust compilation target triple
  }
}

// Generates the following tasks:
// - cargoBuild-libhello-i686-pc-windows-msvc
// - cargoBuild-libhello
// - cargoBuildAll

// Built artifacts for each target will be available at this path:
// <gradleProjectDir>/build/rustLibs/<cargoTarget>
```

### Examples

- [android-app](https://github.com/rushiiMachine/rust-gradle-plugin/tree/master/examples/android-app):
  This is a basic Android-only app setup with the `com.android.application` Gradle plugin. Contains
  one Cargo project that is built and automatically bundled into the APK as a JNI library using this
  RGP plugin.
- [android-library](https://github.com/rushiiMachine/rust-gradle-plugin/tree/master/examples/android-library):
  Same as above, except it uses the `com.android.library` Gradle plugin to bundle everything into an
  AAR that is deployable to a Maven server.
- [desktop-app](https://github.com/rushiiMachine/rust-gradle-plugin/tree/master/examples/desktop-app):
  This is the same JNI library, except it is configured to build for the host's default Cargo target
  to run a JVM app through Gradle using the built-in `application` Gradle plugin.

## Credits

- Heavily based on [rust-android-gradle](https://github.com/mozilla/rust-android-gradle) for
  Android. Used code is licensed under the same Apache 2.0 license.
- https://github.com/stardust-enterprises/gradle-rust
- https://github.com/cortinico/kotlin-gradle-plugin-template
- https://mozilla.github.io/firefox-browser-architecture/experiments/2017-09-06-rust-on-ios.html
