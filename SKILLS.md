# OBD Wizard - Project Expert Guide & Skills

Welcome to the **OBD Wizard** project repository. This document serves as the definitive technical
specification and knowledge base for the application. Any AI model or developer referencing this
file will gain immediate expert-level context on the codebase architecture, OBD-II communication
protocol, telemetry mathematical models, persistent storage, and custom UI components.

---

## 1. Project Overview & Domain

**OBD Wizard** is a specialized Android application designed for motorcycles and vehicles (
specifically calibrated for Royal Enfield 350 and similar motorcycles) communicating with Bluetooth
OBD-II ELM327 adapters.

Key capabilities:

- **Real-Time Telemetry Dashboard:** Live monitoring of Engine RPM, Vehicle Speed, Throttle
  Position, Engine Temperature, Manifold Absolute Pressure (MAP), Intake Air Temperature (IAT), O2
  Sensor Voltage, ECU Battery Voltage, Engine Load, Ignition Timing Advance, and Fuel Level.
- **Derived Metrics & Calculations:** Instantaneous Gear estimation, Speed-Density fuel flow
  calculation (L/h), instantaneous and session average fuel economy (km/L), battery charging health
  diagnostics, and Air-Fuel mixture status (LEAN, OPTIMAL, RICH).
- **Performance Timers:** Automated 0–40 km/h, 0–60 km/h, 0–80 km/h, and 0–100 km/h acceleration
  timers.
- **Diagnostic Trouble Codes (DTC):** Reading stored DTC codes (Mode 03), translating codes via a
  local JSON dictionary (`dtc_dictionary.json`), and clearing codes / resetting MIL (Mode 04).
- **Trip Management & Analytics:** Recording trips with time-series telemetry snapshots, aggregating
  trip performance metrics (max speed, max RPM, fuel consumed, best acceleration times), persisting
  records in Room Database, and visualizing historical trips with MPAndroidChart.

---

## 2. Tech Stack & Configuration

- **Language:** Java 11
- **Android SDK:**
    - `minSdk`: 36
    - `targetSdk`: 37
    - `compileSdk`: 37
- **Build System:** Gradle with version catalog (`gradle/libs.versions.toml`), Android Gradle
  Plugin (AGP) 9.4.0.
- **UI Architecture:** XML Layouts with **ViewBinding**, AndroidX Navigation Component, Navigation
  Drawer (`NavigationView`), Custom Canvas Views, and MPAndroidChart (`v3.1.0`).
- **Persistence:** Room Database (`2.6.1`) with custom TypeConverters (`TripConverters`) for
  serializing time-series snapshots and lists.
- **Async & Concurrency:** Java `ExecutorService` (single-thread background command queue) and
  `ScheduledExecutorService` (60ms polling loop).

---

## 3. Core Architecture & Package Structure

```
com.diy.obdwizard/
├── MainActivity.java                # Main container activity, Navigation Drawer, Bluetooth & OBD lifecycle manager
├── bluetooth/
│   └── BluetoothConnection.java     # Bluetooth RFCOMM SPP socket management & command/response stream I/O
├── dtc/
│   └── DtcHelper.java               # DTC dictionary asset loader, Mode 03 parser, and Mode 04 clear handler
├── obd/
│   ├── ObdConnectionManager.java    # Background execution queue, 60ms polling scheduler, pause/resume state
│   ├── ObdParser.java               # SAE J1979 Mode 01 PID parsing algorithms & physics/math calculation engine
│   ├── RePidEngine.java             # ELM327 initialization commands, polling sequence, performance timer logic
│   ├── interfaces/
│   │   ├── IObdDataSource.java
│   │   ├── ObdManagerCallbacks.java
│   │   └── TelemetryUpdateListener.java
│   └── models/
│       └── MotorcycleTelemetry.java # Comprehensive telemetry data transfer object (raw PIDs + derived metrics)
├── trip/
│   ├── TripManager.java             # Singleton managing active trip recording, telemetry sampling, peak tracking
│   ├── TripRecord.java              # Room Entity representing a completed trip and nested TelemetrySnapshot list
│   └── db/
│       ├── TripConverters.java      # Gson-based TypeConverters for Room DB storing lists
│       ├── TripDao.java             # Room Data Access Object for trips
│       └── TripDatabase.java        # Room Database instance
└── ui/
    ├── activities/
    │   ├── FullScreenChartActivity.java # Landscape chart view for trip analytics
    │   └── TripSummaryActivity.java     # Detailed trip statistics & interactive MPAndroidChart view
    ├── adapters/
    │   └── TripHistoryAdapter.java      # RecyclerView adapter for past trips
    ├── charts/
    │   └── ChartDataHolder.java         # Static data holder for passing chart payloads between activities
    ├── fragments/
    │   ├── AboutFragment.java
    │   ├── AdvancedDashFragment.java    # Secondary ECU telemetry & timing statistics fragment
    │   ├── DashboardFragment.java       # Primary gauge dashboard fragment with RpmSpeedGaugeView & ArcGaugeView
    │   ├── DtcFragment.java             # DTC diagnostics UI fragment
    │   └── TripHistoryFragment.java     # History list fragment
    └── views/
        ├── ArcGaugeView.java            # Custom circular arc gauge component
        ├── BikeGearView.java            # Custom gear indicator view (N, 1, 2, 3, 4, 5)
        ├── RetroPointerDrawer.java      # Retro dial needle drawing helper
        └── RpmSpeedGaugeView.java       # High-performance custom canvas RPM & Speed gauge view
```

---

## 4. Subsystem Deep Dive

### 4.1 Bluetooth & ELM327 Communication (`BluetoothConnection`, `ObdConnectionManager`,
`RePidEngine`)

- **Protocol:** Standard Bluetooth Classic RFCOMM using the Bluetooth Serial Port Profile (SPP)
  UUID: `00001101-0000-1000-8000-00805F9B34FB`.
- **ELM327 Initialization Sequence (`RePidEngine.initializeElm()`):**
    1. `ATZ` - Reset ELM327 adapter to factory defaults.
    2. `ATE0` - Echo off (disable character echoing).
    3. `ATL0` - Linefeeds off.
    4. `ATS0` - Spaces off (remove whitespace separators in hex responses for easier parsing).
    5. `ATH0` - Headers off (hide OBD CAN header bytes).
    6. `ATSP0` - Set protocol to automatic search.
    7. `0100` - Request supported PIDs (01-20) to verify ECU response (`4100`).
- **Polling Loop:** `ObdConnectionManager` schedules telemetry polling every **60 ms** (
  `ScheduledExecutorService`). During synchronous operations (such as reading or clearing DTCs),
  `isPaused` is set to `true` to prevent command collisions on the single-thread command queue.

### 4.2 OBD-II PID Parsing & Mathematical Models (`ObdParser`)

All Mode 01 PIDs follow the SAE J1979 standard:

- **RPM (`010C`):** `((A * 256) + B) / 4` (Range: 0 - 16,383.75 RPM)
- **Speed (`010D`):** `A` km/h (Range: 0 - 255 km/h)
- **Throttle (`0111`):** `(A * 100) / 255` % (Range: 0% - 100%)
- **Coolant / Engine Temp (`0105`):** `A - 40` °C (Range: -40 to 215 °C)
- **MAP (`010B`):** `A` kPa (Range: 0 - 255 kPa)
- **IAT (`010F`):** `A - 40` °C
- **O2 Sensor Voltage (`0114`):** `A / 200` V (Range: 0 - 1.275 V)
- **Control Module Voltage (`0142`):** `((A * 256) + B) / 1000` V
- **Engine Load (`0104`):** `(A * 100) / 255` %
- **Timing Advance (`010E`):** `(A / 2) - 64` degrees
- **Run Time (`011F`):** `(A * 256) + B` seconds
- **Distance with MIL (`0121`):** `(A * 256) + B` km
- **Fuel Level (`012F`):** `(A * 100) / 255` %

#### Derived Algorithms:

- **Gear Estimation (`calculateGear`):** Computes ratio `(Speed_Kmh / RPM) * 1000`. Calibrated for
  Royal Enfield 350 gear ratios across 1st through 5th gear (returns `"N"` if speed < 3 km/h or
  RPM < 800).
- **Fuel Flow & Economy (Speed-Density Method):**
    - Approximates Mass Air Flow (MAF) using the Ideal Gas Law (`PV = nRT`) based on RPM, MAP, IAT (
      converted to Kelvin), engine displacement (`0.349 L` for RE 350), and volumetric efficiency (
      `VE = 0.80`).
    - Computes fuel mass flow using stoichiometric air-fuel ratio (`AFR = 14.7`) and gasoline
      density (`740 g/L`) to yield fuel flow in Liters per hour (L/h).
    - Instantaneous Fuel Economy: `Speed_Kmh / FuelFlow_Lph` (km/L).
- **Battery Health Check:** Flags `isBatteryChargingPoor = true` if RPM > 1500 but voltage < 13.5V (
  indicating stator/rectifier wear).
- **O2 Mixture Status:** Evaluates O2 voltage (`< 0.3V` = LEAN, `> 0.6V` = RICH, else OPTIMAL).

### 4.3 Diagnostic Trouble Codes (`DtcHelper`)

- **Dictionary Asset:** Loads `assets/dtc_dictionary.json` containing standard OBD-II diagnostic
  fault codes mapped to human-readable descriptions.
- **Mode 03:** Requests stored DTC codes (`03`), parses hex response starting with `43`, translating
  chunks into standard `PXXXX` codes.
- **Mode 04:** Sends clear command (`04`), verifying success upon receiving `OK` or `44`.

### 4.4 Trip Recording & Persistence (`TripManager`, `TripRecord`, Room DB)

- **Trip Lifecycle:** `TripManager` tracks recording state, records time-series `TelemetrySnapshot`
  objects at polling intervals, updates peak extremes (max speed, max RPM, max temp, max load,
  min/max voltage), and tracks best 0-40, 0-60, 0-80, 0-100 km/h acceleration times.
- **Persistence:** Uses Room DB (`TripDatabase`) with Gson `TripConverters` to persist `TripRecord`
  entities asynchronously on background threads.

---

## 5. Guidelines for AI Maintenance & Extension

When modifying or extending this codebase:

1. **Thread Safety:** Never perform blocking network or Bluetooth I/O on the main thread. Always
   dispatch commands through `ObdConnectionManager` / `commandExecutor`.
2. **UI Updates:** Ensure all telemetry callbacks update UI fragments/views strictly on the main
   thread (`runOnUiThread`).
3. **ViewBinding:** Always use ViewBinding (`*Binding`) when interacting with layouts in fragments
   and activities.
4. **OBD Command Protocol:** When adding new PIDs, ensure proper hex parsing in `ObdParser` and
   handle malformed or missing ECU responses gracefully without crashing the polling loop.
