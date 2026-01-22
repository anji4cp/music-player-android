# 🎵 Android Music Player

A simple yet modern **Android Music Player** built with **Java** and **Android Studio**.  
This project is designed as a learning project and portfolio, featuring a clean UI, global media playback, and smooth navigation.

---

## ✨ Features

- 🎧 Play local audio files from device storage
- ▶️ Play / Pause / Next / Previous controls
- 🎼 Global `MediaPlayer` (music keeps playing when switching tabs)
- 🧩 Mini Player (visible in Library, expandable to Home)
- 🏠 Full Player screen (Home)
- 📚 Music Library list
- 🎨 Album art extracted directly from audio files
- 🧭 Bottom Navigation (Home & Library)
- 🎞 Smooth tab transition animations
- 📱 Responsive UI for different screen sizes

---

## 📱 Screens

- **Home**
  - Large album art
  - Song title & artist
  - Seek bar
  - Full playback controls

- **Library**
  - List of songs from device storage
  - Tap song to play
  - Mini player appears at the bottom

---

## 🛠 Tech Stack

- **Language**: Java  
- **IDE**: Android Studio  
- **UI**: XML (ConstraintLayout)  
- **Architecture**:
  - `MainActivity` (navigation & mini player)
  - `HomeFragment` (full player)
  - `LibraryFragment` (song list)
  - `PlayerManager` (global MediaPlayer handler)

---

## 🔐 Permissions

The app requires the following permissions:

- Android 13+  
  - `READ_MEDIA_AUDIO`
- Android 12 and below  
  - `READ_EXTERNAL_STORAGE`

These permissions are used **only** to read local audio files.
