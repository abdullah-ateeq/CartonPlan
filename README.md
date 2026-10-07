# CartonPlan 📦

**CartonPlan** is a fast, modern, and offline native Android application built with **Jetpack Compose** and **Material Design 3**. It simplifies logistics, inventory planning, and packaging calculations by determining exact carton requirements for any quantity of identical items.

---

## ✨ Features

- 📦 **Carton & Packaging Calculation**
  - **Required Cartons**: Computes total cartons needed based on item quantity and capacity per carton.
  - **Full vs. Partial Breakdown**: Clearly splits total required cartons into fully packed cartons and partial cartons.
  - **Final Carton Inspection**: Displays exact item count and fill percentage for the last/partial carton.
  - **Unused Capacity & Utilization**: Calculates total packable capacity, unused space, and overall percentage utilization.
  - **Planning Allowance**: Optional buffer percentage (0% to 50%) for excess/spare item calculations.
  - **Extra Cartons**: Add fixed extra cartons to packaging plans for safe shipping buffers.

- 📊 **Visual Diagrams & Step-by-Step Formulas**
  - **2D Carton Grid Visualization**: Visual representation distinguishing full cartons from partial cartons.
  - **Mathematical Formula Breakdown**: Transparent mathematical formulas explaining how every metric is calculated.
  - **Preset Quick-Chips**: Tap-to-apply preset values for fast estimation.

- 💾 **History, Favorites & Export**
  - **Local Saved Calculations**: Save calculations locally with custom project/item names.
  - **Search & Filter**: Filter saved records by keyword or favorite status.
  - **Copy & Share**: Formatted plain-text summary ready to copy or share via messaging and email apps.

- ⚙️ **Customization & Preferences**
  - **Theme Support**: System default, Light theme, and Dark theme.
  - **Material You Dynamic Colors**: Adapts seamlessly to user system wallpaper colors (Android 12+).
  - **Default Parameters**: Customize default item counts, carton capacities, and decimal precision.
  - **UI Visibility Toggles**: Customize visible cards (Diagrams, Formulas, Breakdowns, Utilization, Allowance).
  - **Haptic Feedback**: Tactile feedback on actions and input changes.

- 🔒 **100% Offline & Private**
  - Zero network permissions required (`INTERNET` permission is not requested).
  - All data remains strictly on your local device.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) pattern
- **State Management**: Kotlin Coroutines, `StateFlow`, and `SharedFlow`
- **Navigation**: Jetpack Navigation Compose
- **Data Storage**: Jetpack DataStore Preferences + JSON Codec
- **Build Tooling**: Gradle Kotlin DSL (`build.gradle.kts`), Gradle Version Catalog (`libs.versions.toml`)

---

## 📱 Application Structure

The app consists of four main screens accessible via bottom navigation:
1. **Calculator Screen**: Main workspace for entering quantities, picking presets, calculating results, visualizing carton grid, and reading formulas.
2. **History Screen**: Searchable list of saved calculations with quick filter, favorite toggles, copy, and delete options.
3. **Settings Screen**: Preferences for theme mode, default calculation inputs, display toggles, and data clearing.
4. **About Screen**: App version overview, offline security guarantee, key features list, and packaging disclaimer.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer recommended.
- **JDK**: Java 11 or higher.
- **Android Device / Emulator**: Running Android 7.0 (API Level 24) or higher.

### Building & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/abdullah-ateeq/CartonPlan.git
   cd CartonPlan
   ```

2. **Open in Android Studio:**
   - Open Android Studio and select **File > Open...**
   - Select the `CartonPlan` directory.

3. **Build the Project:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests:**
   ```bash
   ./gradlew test
   ```

---

## 📐 Calculation Rules & Formulas

| Metric | Formula | Description |
|---|---|---|
| **Planning Quantity** | $\lceil TotalItems \times (1 + \frac{Allowance\%}{100}) \rceil$ | Total items including planning allowance buffer |
| **Required Cartons** | $\lceil \frac{PlanningQuantity}{ItemsPerCarton} \rceil$ | Total cartons needed to pack all items |
| **Full Cartons** | $RequiredCartons - (PlanningQuantity \bmod ItemsPerCarton \neq 0 ? 1 : 0)$ | Count of 100% filled cartons |
| **Partial Cartons** | $PlanningQuantity \bmod ItemsPerCarton \neq 0 ? 1 : 0$ | Count of partially filled cartons (0 or 1) |
| **Final Carton Items** | $PlanningQuantity \bmod ItemsPerCarton \neq 0 ? (PlanningQuantity \bmod ItemsPerCarton) : ItemsPerCarton$ | Number of items in the final carton |
| **Items Packed** | $RequiredCartons \times ItemsPerCarton$ | Total carton capacity used |
| **Unused Capacity** | $ItemsPacked - PlanningQuantity$ | Empty space in cartons |
| **Utilization %** | $\frac{PlanningQuantity}{ItemsPacked} \times 100$ | Space efficiency percentage |

---

## 📄 License

```text
Copyright 2025 CartonPlan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
