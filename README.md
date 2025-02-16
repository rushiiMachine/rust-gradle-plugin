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

`./src/main/rust/Cargo.toml`:

```toml
[package]
name = "libhello" # Irrelevant

[lib]
name = "hello" # The libName property as specified to RGP
crate-type = ["cdylib"] # Important!
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

## Targets

Each specified Cargo project target needs to be installed via `rustup`. This plugin
does not handle installing targets. That being said, if you do not execute a build
task for a target that is not installed, no error will occur. The only time verifying
targets will happen is when Cargo is invoked to build a target.

## Default

You can specify a target named `default` to make Cargo build the default target for
the platform. This is mainly useful for running tests requiring the native lib, since
it will compile the target native to the platform, which can be loaded by the JVM.
As long as you have installed Rust, the default target will be preinstalled by `rustup`.

```kotlin
rust {
  projects {
    create("abc") {
      // Build for the host's default target
      targets.addAll("default")
    }
  }
}
```

Note that any default target override specified in
Cargo's [`config.toml`](https://doc.rust-lang.org/cargo/reference/config.html#buildtarget)
will override the default target and compile for it instead.

### Android

Using Android targets requires applying the Android Gradle Plugin (AGP) to the same
Gradle project via the `com.android.application` or `com.android.library` plugins.
Furthermore, this also requires the Android NDK to be installed, and version specified to AGP:

`build.gradle.kts`:

```kotlin
android {
  // Tie this project to a specific NDK version.
  ndkVersion = "28.0.13004108"

  // Or, use the latest installed NDK version.
  // Use caution with this, as different NDK versions may produce
  // different and/or possibly broken builds. It is always better
  // to explicitly specify the target NDK version.
  ndkVersion = sdkDirectory.resolve("ndk").listFilesOrdered().last().name
}

rust {
  projects {
    create("abc") {
      // ...
      targets.addAll(
        "armv7-linux-androideabi", "aarch64-linux-android",
        "i686-linux-android", "x86_64-linux-android",
      )
    }
  }
}
```

#### Supported NDK versions

TODO

### iOS

Work in progress.

### macOS

Work in progress.

### Native or other targets

Assuming your target is not covered by one of the sections above, this plugin
assumes any other target you specify will succeed without any extra fixes.
This likely means you're trying to build for the current platform.

If you need to do some sort of toolchain massaging, add compiler/linker flags, etc.,
see the [TODO](#todo) section.

## Credits

- Heavily based on [rust-android-gradle](https://github.com/mozilla/rust-android-gradle) for
  Android. Used code is licensed under the same Apache 2.0 license.
- https://github.com/stardust-enterprises/gradle-rust
- https://github.com/cortinico/kotlin-gradle-plugin-template
- https://mozilla.github.io/firefox-browser-architecture/experiments/2017-09-06-rust-on-ios.html
