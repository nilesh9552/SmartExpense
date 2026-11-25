# Smart Expense – Android Expense Tracker

A modern, production-quality, offline-first personal expense tracker built strictly using **100% Java and XML layouts** for Android Studio.

## Highlights & Features

- **Language & UI**: 100% Java (no Kotlin) and native XML layouts styled with Material Design 3.
- **Offline First**: Zero backend or registration required. All data persists locally in SQLite.
- **Architecture**: Clean MVVM architecture separating Models, SQLiteOpenHelper DAO, Repository with background worker threads, ViewModels, and UI.
- **Dashboard**:
  - Monthly spending overview with real-time budget progress bar.
  - Dynamic status indicator (On Track, Approaching at 80%, Over Budget at 100%).
  - Today's spending and quick-action shortcuts.
  - Category breakdown summary with percentage indicators.
  - Recent transactions with direct edit/delete capabilities.
- **Add / Edit Expense**:
  - Form validation (non-empty name, amount > 0, category and payment method selection).
  - DatePicker dialog integration.
  - Category selection: Food, Transport, Shopping, Bills, Health, Education, Entertainment, Other.
  - Payment method selection: UPI, Cash, Debit Card, Credit Card, Bank Transfer.
  - Optional remarks/notes field.
- **Transaction History**:
  - Chronological expense list.
  - Live search by title or notes.
  - Filter by category and date range (Today, This Week, This Month, Custom Date Range).
  - Edit and Delete with confirmation dialogs.
  - Empty-state illustrations.
- **Analytics & Visual Charts**:
  - Timeframe selection (Week, Month, Year, All Time).
  - Key metrics: Total spent, Daily average, Highest spending category, Transaction count.
  - Custom hardware-accelerated Donut Pie Chart (`DonutPieChartView`) and Rounded Bar Chart (`BarChartView`) requiring zero external third-party chart dependencies.
  - Month-over-month comparison card.
- **Budget Management**:
  - Overall monthly budget with warning notifications.
  - Category-specific budgets with custom limits.
- **Settings & Data Management**:
  - Multi-currency selection (USD `$`, INR `₹`, EUR `€`, GBP `£`, JPY `¥`, CAD `CA$`, AUD `AU$`).
  - Dark mode support (System Default, Light, Dark).
  - CSV Export with Android system share sheet via FileProvider.
  - About dialog and privacy information.

---

## Project Structure

```
SmartExpense/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/smartexpense/
│       │   ├── SmartExpenseApp.java
│       │   ├── activities/
│       │   │   ├── MainActivity.java
│       │   │   ├── AddEditExpenseActivity.java
│       │   │   └── CategoryBudgetActivity.java
│       │   ├── fragments/
│       │   │   ├── DashboardFragment.java
│       │   │   ├── HistoryFragment.java
│       │   │   ├── AnalyticsFragment.java
│       │   │   └── SettingsFragment.java
│       │   ├── adapters/
│       │   │   ├── ExpenseAdapter.java
│       │   │   ├── CategorySummaryAdapter.java
│       │   │   └── CategoryBudgetAdapter.java
│       │   ├── database/
│       │   │   ├── DatabaseHelper.java
│       │   │   └── ExpenseDao.java
│       │   ├── models/
│       │   │   ├── Expense.java
│       │   │   ├── CategorySummary.java
│       │   │   ├── CategoryBudget.java
│       │   │   └── MonthlyComparison.java
│       │   ├── repositories/
│       │   │   └── ExpenseRepository.java
│       │   ├── viewmodels/
│       │   │   ├── ExpenseViewModel.java
│       │   │   └── ExpenseViewModelFactory.java
│       │   ├── views/
│       │   │   ├── DonutPieChartView.java
│       │   │   └── BarChartView.java
│       │   └── utils/
│       │       ├── CurrencyHelper.java
│       │       ├── DateUtils.java
│       │       ├── NotificationUtils.java
│       │       ├── CsvExportHelper.java
│       │       └── PreferenceManager.java
│       └── res/
│           ├── color/
│           ├── drawable/
│           ├── layout/
│           ├── menu/
│           ├── mipmap-anydpi-v26/
│           ├── values/
│           ├── values-night/
│           └── xml/
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## Build and Run Instructions

### 1. Open in Android Studio
1. Launch **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala or newer).
2. Click **Open** and select `/home/nilesh/.gemini/antigravity/scratch/SmartExpense`.
3. Wait for Android Studio to sync Gradle and download dependencies.

### 2. Run the Application
1. Connect an Android device with USB debugging enabled or launch an Android Virtual Device (AVD) running Android 8.0 (API 26) or higher.
2. Select `app` in the run configuration dropdown.
3. Click the green **Run** button (or press `Shift + F10`).

### 3. Generate APK
- **Debug APK**:
  - In Android Studio: Go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
  - The generated APK will be in `app/build/outputs/apk/debug/app-debug.apk`.
- **Release APK**:
  - Go to **Build** > **Generate Signed Bundle / APK...**
  - Choose **APK**, select or create your keystore, and click **Finish**.
