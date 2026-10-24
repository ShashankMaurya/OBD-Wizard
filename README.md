# OBD Wizard 🚗🏍️🔌

<p align="center">
  <b>Advanced Open-Source OBD-II Vehicle & Motorcycle Telemetry Scanner for Android</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform Android">
  <img src="https://img.shields.io/badge/Language-Java_11-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="Language Java">
  <img src="https://img.shields.io/badge/Min_SDK-36-blue?style=flat-square" alt="Min SDK 36">
  <img src="https://img.shields.io/badge/Target_SDK-37-orange?style=flat-square" alt="Target SDK 37">
  <img src="https://img.shields.io/badge/License-MIT-green.svg?style=flat-square" alt="License MIT">
  <img src="https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square" alt="Build Status">
  <a href="https://github.com/your-username/OBD-Wizard/releases/latest"><img src="https://img.shields.io/github/v/release/your-username/OBD-Wizard?label=Download%20APK&color=green" alt="Latest Release"></a>
</p>

---

## 📖 About OBD Wizard

**OBD Wizard** is a comprehensive, high-performance, open-source Android application designed for
car and motorcycle enthusiasts, DIY mechanics, and drivers who want deep, real-time insights into
their vehicle's health and performance.

By connecting to standard **ELM327 Bluetooth / Wi-Fi OBD-II adapters**, OBD Wizard bridges your
vehicle's Electronic Control Unit (ECU) directly to your Android device with custom-crafted,
responsive dashboards, diagnostic trouble code (DTC) analysis, and comprehensive trip logging.

---

## ✨ Key Features

### 🔌 Robust OBD-II Communication Manager (`ObdConnectionManager`)

- Handles Bluetooth pairing, connection lifecycle, and command queueing (`AT` commands and standard
  PID queries).
- Asynchronous polling engine for real-time telemetry streaming without blocking the UI thread.
- Configurable command intervals and automatic reconnection strategies.

### 🏎️ High-Performance Live Dashboards (`DashboardFragment`, `AdvancedDashFragment`)

- **Custom Canvas Gauges:** Smooth, high-FPS retro and futuristic custom views (`RpmSpeedGaugeView`,
  `ArcGaugeView`, `BikeGearView`) designed for cars and motorcycles.
- **Core Telemetry:** Live RPM, vehicle speed, engine load, coolant temperature, battery voltage,
  throttle position, and calculated gear ratio.
- **Advanced Dash:** Secondary ECU telemetry, performance timers, and live charts.

### 🔍 Diagnostic Trouble Codes (DTC) Scanner (`DtcFragment`, `DtcHelper`)

- Scan and retrieve stored, pending, and permanent diagnostic trouble codes (Modes 03/07/0A).
- Built-in offline **DTC Dictionary** (`dtc_dictionary.json`) providing instant descriptions and
  troubleshooting hints.
- Clear DTCs and reset the Check Engine Light (CEL) with confirmation safeguards (Mode 04).

### 📈 Trip Logging & History (`TripManager`, Room DB)

- Automatic trip recording tracking duration, distance, max/average speed, max RPM, and fuel
  consumption.
- Persistent local storage powered by **Android Room Database**.
- Interactive trip summary analysis and full-screen charts powered by **MPAndroidChart**.

### 🚙 Multi-Vehicle Profile Management (`ProfileManager`)

- Custom profiles supporting both **Cars** and **Motorcycles**.
- Configurable vehicle parameters: weight, engine displacement, redline RPM, fuel tank capacity, and
  blueprint icons.

---

## 🛠️ Architecture & Tech Stack

OBD Wizard is built following clean architectural principles and robust Android development
standards:

- **Language:** Java 11
- **Architecture:** Modular Service/Manager pattern combined with Android Architecture Components (
  Repositories, DAOs, Room, ViewBinding).
- **UI & Navigation:** Material 3 Design, Navigation Drawer (`NavigationView`), Fragments, and
  custom Canvas 2D views.
- **Persistence:** Android Room Persistence Library (`TripDatabase`, `VehicleProfileDao`,
  `TripDao`).
- **Charting:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for high-performance
  telemetry graphs and trip statistics.
- **Serialization:** Google Gson for JSON parsing (DTC dictionary).

---

## 📂 Project Structure

```tree
com.diy.obdwizard/
├── bluetooth/          # Bluetooth connection & socket management
├── dtc/                # Diagnostic trouble codes helper & dictionary parsing
├── obd/                # ELM327 command parser, telemetry engine, and data sources
├── trip/               # Trip recording, profile management, and Room database
├── ui/
│   ├── activities/     # Full-screen charts and trip summary activities
│   ├── adapters/       # RecyclerView adapters for trip history & profiles
│   ├── charts/         # Chart data holders & formatters
│   ├── fragments/      # Navigation fragments (Dashboard, DTC, History, Profile, About)
│   └── views/          # Custom gauge and meter views (RPM/Speed, Arc, Gear)
└── MainActivity.java   # Main container & navigation controller
```

---

## 🚀 Getting Started

### 📱 Installing from Releases (Sideloading)

If you downloaded the prebuilt APK from
the [Releases](https://github.com/your-username/OBD-Wizard/releases) page:

1. Download the latest `OBD-Wizard-v1.0.0.apk` from the Releases section.
2. Open the downloaded file on your Android device.
3. If prompted, allow your browser or file manager permission to **"Install unknown apps"** in your
   system settings.
4. Tap **Install** and launch the app.

### Prerequisites

- **Android Studio:** Arctic Fox / Hedgehog or newer (Gradle 8.x+).
- **Hardware:** An ELM327 OBD-II Bluetooth adapter (v1.5 or v2.1 recommended) plugged into your
  vehicle's OBD-II port.
- **Android Device:** Running Android 14 (API 36) or higher.

### Building & Running from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/OBD-Wizard.git
   ```
2. Open the project folder in Android Studio.
3. Sync Gradle and run the app (`:app:assembleDebug`) on your physical Android device with Bluetooth
   enabled.

---

## 📚 References & External Open-Source Projects

While developing OBD Wizard, we drew inspiration and architectural guidance from several fantastic
open-source projects and official specifications in the Android and OBD-II community:

1. **[pires/obd-java-api](https://github.com/pires/obd-java-api):** Reference implementation for
   parsing ELM327 PID response formulas and OBD-II command structures.
2. **[PhilJay/MPAndroidChart](https://github.com/PhilJay/MPAndroidChart):** The industry-standard
   charting library used for rendering live telemetry graphs and historical trip speed/RPM curves.
3. **[Official Android Developer Documentation](https://developer.android.com/):** Guidelines on
   Bluetooth permissions (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`), Material 3 design systems, and
   Room Database architecture.
4. **SAE J1979 Standards:** Specifications for diagnostic test modes and PID definitions (RPM,
   Speed, Coolant Temp, Throttle, etc.).

---

## 📄 License

This project is open-source under the [MIT License](LICENSE). Feel free to fork, contribute, and
build upon OBD Wizard for your automotive and motorcycle DIY projects!
