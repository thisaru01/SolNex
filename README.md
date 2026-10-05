# SolNex

SolNex is a smart solar microgrid management system. It provides a browser-based operations console and a native Android client backed by a central ASP.NET Core REST API and MongoDB.

## What is implemented

### Web console

The React/Vite web application is intended for **Backoffice** and **GridOperator** users. It currently includes:

- JWT login and prosumer registration
- Role-protected routes
- Dashboard
- User and prosumer management
- User activation, deactivation, role changes, and deactivation-request review
- Solar station creation, editing, activation/deactivation, details, schedules, battery slots, and map views
- Booking slot and reservation views, history, search, and reservation actions
- Reservation approval and rejection
- Transaction list, details, and operational history

### Android application

The native Android app is built with Kotlin and Jetpack Compose. It includes:

- Prosumer login, registration, password reset, profile management, and deactivation requests
- Home, map, reservations, QR, and profile navigation
- Google Maps station discovery
- Reservation creation, editing, cancellation, and history
- Prosumer transaction QR display
- Grid operator QR scanning, transaction verification, and completion
- Local SQLite helpers for user, station, reservation, and transaction data

### API and data

The ASP.NET Core API provides authentication and server-side business logic for:

- Users and account lifecycle
- Solar stations, schedules, availability, and battery slots
- Energy booking slots and reservations
- Reservation approval/rejection
- Energy transaction verification and completion

MongoDB is accessed only by the API. Web and Android clients communicate with the API over HTTP and do not connect directly to MongoDB.

## Architecture

```text
┌──────────────────────────┐       REST/JSON       ┌──────────────────────────┐
│ React + Vite web console │ ────────────────────► │ ASP.NET Core 10 API      │
└──────────────────────────┘                      │ JWT authentication       │
                                                  │ Services/repositories    │
┌──────────────────────────┐       REST/JSON       └────────────┬─────────────┘
│ Kotlin + Compose Android│ ────────────────────────────────────┘
└──────────────────────────┘                                   │ MongoDB.Driver
                                                               ▼
                                                        ┌──────────────┐
                                                        │ MongoDB       │
                                                        │ SolNexDb      │
                                                        └──────────────┘
```

The API is the source of truth for reservations, approvals, and transaction verification. Android SQLite storage is local client data and does not replace the server database.

## Repository layout

```text
SolNex/
├── backend/
│   ├── SolNex.Api/
│   │   ├── Controllers/
│   │   ├── Data/
│   │   ├── DTOs/
│   │   ├── Extensions/
│   │   ├── Models/
│   │   ├── Repositories/
│   │   ├── Services/
│   │   ├── Program.cs
│   │   └── SolNex.Api.csproj
│   ├── SolNex_Reservations_Postman.json
│   └── SolNex_Stations_Postman.json
├── web/
│   └── SolNexWeb/
│       ├── src/
│       │   ├── components/
│       │   ├── pages/
│       │   └── services/
│       ├── package.json
│       └── vite.config.js
├── Android/
│   └── SolNexMobile/
│       ├── app/
│       ├── gradle/
│       └── settings.gradle.kts
└── README.md
```

## Technology stack

- **Backend:** C#, ASP.NET Core 10, JWT bearer authentication, OpenAPI, MongoDB.Driver 3.12
- **Web:** React 19, React Router 7, Vite 8, Tailwind CSS 4, Lucide icons, Google Maps
- **Android:** Kotlin 2.0, Jetpack Compose, Android Gradle Plugin 8.9, Google Maps Compose, ZXing
- **Database:** MongoDB or MongoDB Atlas
- **Target Android SDK:** 35; minimum Android SDK: 24

## Prerequisites

- .NET 10 SDK
- Node.js and npm
- Android Studio with an Android SDK and emulator, or a physical Android device
- MongoDB instance (local or Atlas)
- Google Maps API key for Android map features

## Configuration

### API

The API reads database and JWT settings from `backend/SolNex.Api/appsettings.json` and environment-specific configuration. Set `DatabaseSettings:ConnectionString` to a MongoDB connection string and keep credentials out of source control. The default database name is `SolNexDb`.

The API listens on `http://localhost:5097` when launched with the included development profile used by the clients. OpenAPI is exposed in development.

### Web

The web client defaults to `http://localhost:5097`. To use another API URL, create `web/SolNexWeb/.env.local`:

```dotenv
VITE_API_URL=http://localhost:5097
```

Do not commit local `.env` files or credentials.

### Android

1. Add `MAPS_API_KEY=your_key_here` to `Android/SolNexMobile/local.properties`.
2. For an Android emulator, the API base URL is `http://10.0.2.2:5097`.
3. For a physical device, update `ApiConfig.kt` with an IP address reachable from the device, or use:

   ```text
   adb reverse tcp:5097 tcp:5097
   ```

   and configure the device to use the development API address.

The Android app requires network access and uses camera permission for QR scanning and location permission for map features.

## Running locally

### 1. Start the API

From the repository root:

```powershell
dotnet run --project .\backend\SolNex.Api\SolNex.Api.csproj
```

The API will use the configured MongoDB connection and listen on the development URL.

### 2. Start the web console

```powershell
cd .\web\SolNexWeb
npm install
npm run dev
```

Useful web commands:

```powershell
npm run build   # production build
npm run lint    # ESLint
npm run preview # preview the production build
```

### 3. Run the Android app

Open `Android/SolNexMobile` in Android Studio, configure `local.properties`, sync Gradle, and run the `app` configuration on an emulator or connected device. From PowerShell, a debug APK can also be built with:

```powershell
cd .\Android\SolNexMobile
.\gradlew.bat assembleDebug
```

The APK is generated under `app/build/outputs/apk/debug/`.

## API areas

The API is organized around these route groups:

| Area | Base routes | Examples |
| --- | --- | --- |
| Authentication | `/api/auth` | Login, registration, password reset |
| Users | `/api/users` | User search, profile, roles, activation, deactivation |
| Stations | `/api/stations` | CRUD, map/closest stations, availability, schedules |
| Slots | `/api/slots` | Slot listing, availability, status, reservation |
| Reservations | `/api/reservations` | Create, update, cancel, history, search |
| Approval | `/api/reservations` | Approve or reject a reservation |
| Transactions | `/api/transactions` | Pending/completed history, QR verification, completion |

The API also includes development connectivity endpoints such as `/test-db`. Do not expose diagnostic endpoints publicly without reviewing their access and error responses.

## Typical workflows

### Reservation

1. A prosumer signs in through Android and views stations or available slots.
2. The app creates a reservation through the API.
3. Backoffice or a grid operator reviews pending reservations in the web console.
4. An approved reservation becomes available to the prosumer for the transaction flow.

### Energy transaction

1. The prosumer opens the QR section in the Android app.
2. A grid operator scans the QR code in Operator Mode.
3. The API verifies the transaction and returns its details.
4. The operator confirms the transfer and the API marks it complete.

## Development notes

- Keep business rules in the API services, not in the web or Android clients.
- Keep MongoDB credentials and Google Maps keys in local configuration.
- Clients must use the API rather than connecting directly to MongoDB.
- Generated directories such as `bin/`, `obj/`, `node_modules/`, and Android build output should not be committed.
- The Postman collections in `backend/` can be imported for manual API checks.

## License

No license has been declared for this repository yet.
