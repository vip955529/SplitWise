# SplitMate — Expense Sharing & Bill Splitting App

**SplitMate** is a production-grade, offline-first Android expense-sharing application inspired by Splitwise, built with **Jetpack Compose**, **Clean Architecture + MVVM**, **Hilt**, **Room**, and **Firebase**.

> **"Add expenses once. Split them fairly. Know exactly who owes whom."**

---

## 📱 Completed Features

### 1. 🔐 Authentication & Profile Management
- **Firebase Authentication**: Email and password account registration, sign-in, and password reset.
- **User Profile**: Profile screen displaying user information with inline display name editing.
- **Automatic Sync**: Creates Firestore user profile document and caches locally upon registration.

### 2. 👥 Friends Network
- **Add Friends by Email**: Send friend requests using email addresses.
- **Friends List**: View friends, check status, and remove friends.

### 3. 🏠 Groups & Invite System
- **Create Groups**: Group creation for travel, flatmates, or events with custom descriptions and multi-selected friends.
- **Group Invite Codes**: Automatically generates unique 6-character invite codes (e.g. `SPLIT-8921`).
- **Join Group by Code**: Friends can join existing groups instantly using invite codes.
- **Group Details Hub**: 3 dedicated sections for **Expenses**, **Balances**, and **Members**.

### 4. 💸 Expense Engine & Flexible Splitting
- **4 Split Algorithms**:
  - **Equal Split**: Evenly divides expenses with exact integer remainder distribution.
  - **Exact Split**: Specify exact amounts per participant with sum validation.
  - **Percentage Split**: Divide expenses by exact percentages with 100% total validation.
  - **Shares Split**: Proportional splitting based on assigned shares (e.g., 1 share vs 2 shares).
- **Currency Minor Unit Precision**: Stored as integer minor units (`Long` paise) to prevent floating-point rounding errors.
- **Multi-Currency Support**: Dynamic currency symbol formatting for `INR` (₹), `USD` ($), `EUR` (€), `GBP` (£), `CAD` (CA$), `AED` (AED), and `JPY` (¥).
- **Expense Categories**: 9 categories (`Food & Drinks`, `Transport & Cabs`, `Hotel & Stay`, `Entertainment`, `Groceries`, `Rent`, `Bills & Utilities`, `Shopping`, `Other`) with styled badges.
- **Recurring Expenses**: Support for recurrence frequency (`One-time`, `Daily`, `Weekly`, `Monthly`, `Yearly`).
- **Expense Notes & Comments**: Optional notes attached to expenses.
- **Soft Deletion**: Safely delete expenses with automatic net balance recalculation.

### 5. 🧮 Balance Engine & Debt Simplification
- **Real-Time Net Balances**: Calculates `(Total Paid) - (Total Owed Share)` for every group member.
- **Greedy Debt Reduction Algorithm**: Minimizes the number of transaction transfers needed to settle outstanding group debts.

### 6. 💳 Settlements & Direct UPI Payment Deep Linking
- **Settle Up Dialog**: Record payments between debtors and creditors.
- **Direct UPI Payment Deep Linking**: One-tap **Pay via UPI App** button (`upi://pay?pa=...&pn=...&am=...&cu=INR`) launching Google Pay, PhonePe, Paytm, or BHIM pre-filled with recipient details.

### 7. 📊 Monthly Category Analytics & Live Search
- **Monthly Category Analytics**: Progress bars and spending breakdown per category on the Home Dashboard.
- **Live Search**: Instant expense filtering by description, category, or payer name.

### 8. 🔔 In-App Activity Feed & Logging
- **Automated Event Logging**: Real-time logging when expenses are added or debts are settled.
- **Activity Feed Dialog**: Bell icon on Home TopBar with dynamic unread badge opening chronological activity feed.

### 9. 📤 Shareable Expense Reports
- **Android Share Intent**: One-tap report generator sending formatted group balance summaries via WhatsApp, Email, or SMS (`Intent.ACTION_SEND`).

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture (Presentation, Domain, Data) + MVVM
- **State Management**: StateFlow & Compose State
- **Dependency Injection**: Hilt
- **Local Database**: Room Database (Schema v5)
- **Backend**: Firebase Authentication & Cloud Firestore
- **Asynchronous**: Kotlin Coroutines & Flow
- **Build System**: Gradle Version Catalogs (Kotlin DSL)

```text
                  PRESENTATION
        ┌────────────────────────────┐
        │ Compose UI (Material 3)    │
        │ ViewModel & UI State       │
        └──────────────┬─────────────┘
                       ↓
                   DOMAIN
        ┌────────────────────────────┐
        │ Use Cases                  │
        │ Split Calculator Engine    │
        │ Debt Simplification Engine │
        └──────────────┬─────────────┘
                       ↓
                    DATA
        ┌────────────────────────────┐
        │ Repository Implementations │
        │ Room DAOs & Local Database │
        │ Cloud Firestore            │
        └────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 35 / Android 14+ device or emulator

### Setup Instructions
1. **Clone the repository**:
   ```bash
   git clone https://github.com/vip955529/SplitWise.git
   cd SplitWise
   ```
2. **Firebase Setup**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Enable **Firebase Authentication** (Email/Password) and **Cloud Firestore**.
   - Place `google-services.json` in the `app/` directory.
3. **Build & Run**:
   - Open the project in Android Studio and run Gradle sync.
   - Deploy to your connected device/emulator.

---

## 🧪 Testing

Run unit tests for split algorithms and debt simplification:
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
