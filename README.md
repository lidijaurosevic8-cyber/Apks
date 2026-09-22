# Phone Performance Lab ⚡📱

A professional, high-precision Android hardware monitoring, FPS tracking, and benchmarking laboratory built with **Kotlin** and **Jetpack Compose (Material 3)**.

Designed with a high-contrast cyber-tech aesthetic, Phone Performance Lab provides telemetry and stress testing for Android smartphones and tablets.

---

## 🚀 Download APK

The compiled debug APK is included directly within this project repository:
- **Direct APK**: [`PhonePerformanceLab.apk`](./PhonePerformanceLab.apk) (Root directory)
- **Alternative path**: [`app/build/outputs/apk/debug/app-debug.apk`](./app/build/outputs/apk/debug/app-debug.apk)
- **From AI Studio Interface**: You can also use the AI Studio top/settings menu to export an APK or download the full project archive as a ZIP file.

To install directly to an Android device via ADB:
```bash
adb install PhonePerformanceLab.apk
```

---

## ✨ Features

### 1. 📊 Real-Time Hardware Dashboard
- **CPU Subsystem**: Live processor load percentage, per-core activity gauges, real-time clock frequency in MHz, core count, architecture (ARM64/x86), and governor profile.
- **GPU Subsystem**: GPU vendor, renderer model, and OpenGL ES version detected via offscreen EGL PBuffer context; reports load state with SELinux-compliant truth-in-telemetry.
- **RAM & Memory**: Circular gauge readout tracking active vs. available system memory (GB), process heap allocation (MB), and low-memory state flags.
- **Battery & Power**: Live charge level (%), battery voltage (mV), instantaneous current flow (mA), calculated power draw (Watts), and charging status.
- **Thermal Zones**: Battery and SoC thermal sensors with Android 11+ PowerManager Thermal Headroom & Status API, plus thermal zone sysfs fallbacks.
- **Battery Health Estimator**: Coulomb-counting algorithm estimating effective capacity vs. design capacity, cycle count tracking, and health status indicators.

### 2. 📈 Live Telemetry Graphs & FPS Meter
- **Multi-Metric Graphs**: Canvas-rendered curves for CPU load, CPU frequency, RAM allocation, battery temperature, and charge percentage with selectable timeline windows (10s, 30s, 1m, 5m).
- **Choreographer-Backed FPS Meter**: Real-time frame delivery analysis measuring:
  - Instantaneous FPS & Frame time (ms)
  - Rolling average FPS
  - 1% Low (minimum) FPS
  - Peak frame rates
  - Frame drop / spike counter (>20ms threshold)
- **Interactive Workload Stress Viewport**: 3D wireframe mesh projection and 150+ particle physics simulations to verify rendering throughput under load.

### 3. 🧪 Benchmark Suite
- **CPU Multi-Core Benchmark**: Stresses all available processor cores simultaneously with computational loads:
  - Parallel Prime Sieve (Sieve of Eratosthenes)
  - Cryptographic SHA-256 rounds
  - 3x3 / 4x4 matrix transformations
- **GPU Stress Test**: Evaluates graphics presentation stability, frame times, and rendering throughput.
- **Combined Stress Test**: Full-spectrum composite stress workload with automated measurement of:
  - Thermal increase ($\Delta$ Temp °C)
  - Battery consumption delta
  - Thermal throttling detection

### 4. 🗄️ Historical Tracking & Export
- **Local Persistence**: Powered by Android **Room** database with SQLite backing.
- **Side-by-Side Comparison**: Select and contrast multiple benchmark runs with comparative visualizer bars.
- **Data Export**: Export your benchmark results directly via the Android system share sheet in:
  - **CSV** (Comma-Separated Values)
  - **JSON** (Structured data with full metric breakdown)
  - **Plaintext** (Formatted executive summary)

### 5. 🔍 Device Specifications & Hardware Catalog
- **SoC & Platform**: Chipset model, board, hardware platform, Linux kernel version, ABI support, and Android OS / SDK build ID.
- **Display Details**: Screen resolution (px), pixel density (DPI), calculated physical diagonal (inches), and dynamic refresh rates (60Hz, 90Hz, 120Hz, etc.) queried via `DisplayManager`.
- **Storage Partitions**: Internal storage total capacity, free space, and active allocation percentage.
- **Sensor Inventory**: Catalog of all onboard sensors (accelerometers, gyroscopes, magnetometers, ambient light, barometer, step counters) with vendor, resolution, and power consumption ratings.

### 6. 🔔 Optional Background Monitoring Service
- Foreground notification service providing live hardware stats (CPU load, RAM usage, battery temp) in the Android notification drawer.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture / MVVM
  - `HardwareMonitor`: Thread-safe, non-blocking hardware data collection
  - `MainViewModel`: `StateFlow`-driven UI state synchronization
  - `Room Database`: Type-safe persistent storage with KSP
- **Hardware Integration**:
  - `android.hardware.display.DisplayManager`
  - `android.view.Choreographer`
  - `android.os.BatteryManager` & `PowerManager`
  - `android.hardware.SensorManager`
  - `android.opengl.EGL14` & `GLES20`
- **Minimum SDK**: Android 8.0 (API 26)
- **Target / Compile SDK**: Android 14 (API 34)

---

## 🔨 Building from Source

To compile the application using Gradle:

```bash
# Build the debug APK
gradle assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk

# Run unit and Robolectric tests:
gradle :app:testDebugUnitTest
```

---

## 📄 License
This project is open-source under the Apache 2.0 License.
