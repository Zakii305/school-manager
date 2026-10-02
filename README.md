# 🎓 School Manager — Premium Android App

A production-grade School Management application built entirely with **Kotlin, Jetpack Compose, Firebase, and Hilt** — developed and compiled on **Termux** (Android).

## ✨ Features

### Role-Based Dashboards
- **Admin** — Analytics, user CRUD, notice publishing, fee management
- **Teacher** — Attendance taker, gradebook, assignments
- **Student** — Attendance history, results, homework, fee status
- **Parent** — Multi-child switcher, per-child analytics

### Design
- Material 3 with **Deep Indigo + Teal + Amber** palette
- **Glassmorphism** headers on all dashboards
- **Animated donut charts** for attendance visualization
- **Dark mode** support (system-follow)
- **Slide + fade** navigation transitions
- **Lottie** loading animations

### Tech Stack
| Layer | Technology |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Async | Coroutines + Flow |
| Backend | Firebase (Auth, Firestore, Storage, FCM, Analytics) |
| Local Cache | Room + Firestore persistent cache |
| Navigation | Navigation Compose |
| Images | Coil |
| Animations | Lottie Compose |

## 🛠️ Setup on Termux

### Prerequisites
```bash
pkg update && pkg upgrade -y
pkg install -y openjdk-21 kotlin gradle git wget unzip aapt aapt2 android-tools
