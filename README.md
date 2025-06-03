# FRP Diagnostic App for HTC One M9

An Android application for diagnosing and removing Factory Reset Protection (FRP) on HTC One M9 devices running Android 7.0 Nougat.

## Features

- Read Google accounts associated with the device
- Verify FRP status through system queries
- Display information about critical partitions (misc, fsg, cid)
- Detect bootloader status (locked/unlocked)
- Execute ADB commands locally with root permissions

## Technical Details

- Compatible with API 24 (Android 7.0)
- Implemented in Kotlin
- Includes all necessary permissions in AndroidManifest.xml
- Provides a functional APK for testing in controlled environments

## Usage

1. Install the APK on an HTC One M9 device running Android 7.0
2. Grant all requested permissions
3. Use the interface to diagnose FRP status and perform operations

## Security Notice

This application is intended for diagnostic purposes in controlled environments only. Use responsibly.