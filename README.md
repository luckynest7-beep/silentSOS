# SilentSOS 🔒📶 (Disguised Safety Alert App)

SilentSOS is a highly polished, production-ready native Android personal safety application written in **Kotlin** and powered by **Jetpack Compose**. It is disguised as a fully-functioning standard calculator, enabling discreet emergency alerting and evidence archiving under pressure.

---

## 📱 User Journeys & App Behavior

1. **Fully-Functional Disguise**: On launch, the app initiates a realistic, sleek dark-slate mathematical calculator. It performs standard operations (`+`, `-`, `×`, `÷`, `%`, decimals, parenthesis, and clear/delete keys). No emergency indicators, hints, or debug panels are visible nearby.
2. **Settings Lock / Configuration**: Type **`1397`** and click **`=`** on the calculator display. The app silently navigates to the **SilentSOS Config Dashboard**.
3. **Triggering Silent SOS**: Type **`2026`** (default, customizable in configurations) and click **`=`** on the calculator display. The app generates a 150ms tactile vibration confirmation and starts the background distress sequences.
4. **Safety Stop Verification**: While an SOS incident is active, typing the secure Safe Stop PIN (default **`0000`**, customizable) and clicking **`=`** deactivates the emergency triggers, stops further micro recording, sends a secure safety update broadcast, and displays a friendly feedback.

---

## 🛠️ Deep Feature Set & Orchestration

The application utilizes native Android services coordinated under a Clean Architecture framework:

- **GPS Maps Dynamic Tracking link**: Resolves device coordinates through `FusedLocationProviderClient` fallbacks to standard system base antennas (`LocationManager`). It forms a click-to-nav Google Maps tracking link.
- **Microphone Ambient Recording**: Records a high-fidelity **30-second silent audio clip** locally through standard `MediaRecorder` using `.mp4/AAC` encoder blocks.
- **Front Camera Secret Selfie**: Triggers front camera snapshot via `CameraX` minimization-latency pipeline, capturing the perimeter context seamlessly.
- **Telemetry Battery & Time capture**: Integrates real-time device battery management status (`BatteryManager`) and captures highly precise local timestamps.
- **SMS Responder Broadcasts**: Programmatically divides and dispatches distress emergency alerts directly to configured contacts through modern `SmsManager` ports.
- **Incident Data Persistence (Room)**: Uses a secure, local Room SQLite Database (`AppDatabase`) to persist custom emergency contacts, key-value lock settings, and comprehensive media evidence files (saving full selfie files and interactive audio playbacks directly in the internal storage directory).

---

## 📂 Source Code Clean Architecture

We mapped a pristine, modular structure under the main package root:

- **`com.example.MainActivity.kt`**: Coordinates navigation state and edge-to-edge drawing through lightweight Jetpack Compose crossfade views.
- **`com.example.data.entity`**:
  - `Contact.kt`: Represents localized emergency contact tables.
  - `EmergencyEvent.kt`: Comprehensive database index storing battery telemetry, location coords, date-time labels, and captured local file paths.
  - `Setting.kt`: Core database persistence model for passcodes and toggle configurations.
- **`com.example.services`**:
  - `SosService.kt`: Central alert coordinator that spins location, battery, and triggers quick SMS. It handles asynchronous camera snapshots and 30-second recording clips in parallel to prevent UI locking.
  - `ContactService.kt`: Direct native implementation for sending multiple part SMS to contacts.
  - `LocationService.kt`: Standard coordinates tracking client.
  - `CameraService.kt`: Compact CameraX wrapper utilizing the front camera.
  - `AudioService.kt`: Core MediaRecorder wrapper managing silent audio recordings.
  - `BatteryService.kt`: Core BatteryManager telemetry monitor.
- **`com.example.ui.screens`**:
  - `CalculatorScreen.kt`: Exquisite slate calculator keypad centering display values and evaluating math strings safely.
  - `SettingsScreen.kt`: Hidden operations dashboard. Includes instant permissions trackers, contacts database editors, customizable toggles, codes configuration lists, and a historic incident browser with working camera images and working audio playback nodes.
- **`com.example.ui.theme`**: Custom, eye-safe, premium Material 3 dynamic color scheme configs.

---

## 🧑‍💻 How to Present/Demo in the Hackathon

1. Launch the application in the Streaming Emulator.
2. Tap around the calculator. Verify that arbitrary math works flawlessly (e.g. `12 + 48 = 60`).
3. Type **`1397`** and click **`=`** to slide into the Hidden Dashboard.
4. Set up an emergency responder contact name and a number.
5. Review the Permission switches. Click "**Grant Safety Permissions**" to request immediate phone permission setups.
6. Click the back arrow to re-enter Calculator Disguise.
7. Type **`2026`** and click **`=`**. You'll feel a tiny vibration confirmation while the background captures location, battery percentage, does a 30s ambient audio recorder, and captures a front camera selfie.
8. Re-enter Settings with **`1397`** + **`=`**.
9. Scroll to "**Evidence Archive**". Click on the logged incident report.
10. Marvel at the **captured front camera selfie photo**, see the **accurate Google Maps link**, and **click play to listen to your recorded audio clip** in real-time! 🏆
