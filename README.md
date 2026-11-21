# 📻 Nordis FM Radio

Custom FM radio app for Android, built around hardware FM tuner access on different devices

<p align="center">
  <img src="docs/screenshots/s1.png" width="30%" alt="Main screen">
  <img src="docs/screenshots/s2.png" width="30%" alt="Favorites">
  <img src="docs/screenshots/s3.png" width="30%" alt="Settings">
</p>

<p align="center">
  <img src="https://img.shields.io/github/v/release/NordisApps/nordis-fm-radio?include_prereleases&label=release" alt="Release">
  <img src="https://img.shields.io/github/downloads/NordisApps/nordis-fm-radio/total?label=downloads" alt="Downloads">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Material%203-6750A4?logo=materialdesign&logoColor=white" alt="Material 3">
</p>

## ✨ Features

- Power on/off
- Seek forward / backward
- Frequency scale tuning
- Manual frequency entry via keypad
- Band scan, with results saved to a persisted list (DataStore)
- Favorite stations
- RDS (Radio Data System) PS & RT support
- FM recording (pause / stop / cancel) with automatic volume normalization
- Speaker or headphone output
- Settings: band, channel spacing, RDS, mono mode, soft-mute
- English and Russian interface with in-app language switching
- Light and dark theme
- Tiny footprint: the APK is under 2 MB

## 📱 Supported Devices

Fully supported:

- Samsung Galaxy A10s
- Samsung Galaxy M30s

Partially supported:

- Samsung Galaxy S25 Ultra (Korean model): basic FM features work with the original Samsung USB-C earphones. Some features work only partially. Support will be extended and bugs fixed gradually.
- Known limitations: recording may produce silent files, and scanning right after launch may find nothing until the radio is turned off and on again.

Other Samsung phones may work, but compatibility is not guaranteed. Other regional versions of the same model may have no FM service at all.

Support for additional devices is planned.

## ⚠️ Requirements

- Android 8.0 (API 26) or newer
- A device with FM radio hardware support
- Wired headphones connected (used as the antenna)
- On phones without a headphone jack: wired USB-C headphones (original Samsung USB-C earphones are tested)

## 🌐 Language

The interface is available in English and Russian. You can switch the language in Settings. More translations are planned.

## 📥 Download

Grab the latest APK from the [Releases](https://github.com/NordisApps/nordis-fm-radio/releases) page.

This is an early alpha, so expect bugs and rough edges.

## 🐛 Feedback

Found a bug or have a device to add? Please [open an issue](https://github.com/NordisApps/nordis-fm-radio/issues) and include:

- your phone model and Android version
- your firmware region (CSC), if you know it
- what you did and what happened
- what you expected instead

## 📝 License
 
Licensed under the [GNU General Public License v3.0](LICENSE).

## 👤 Author
 
Built by Nordis.
