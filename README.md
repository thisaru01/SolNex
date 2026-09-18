# SolNex

Smart Solar Microgrid Trading System
SE4040 — Enterprise Application Development

A client-server based Smart Solar Microgrid Trading System consisting of:

Web application
Native Android mobile application
Central C# REST Web API
MongoDB NoSQL database

The system allows back-office users, grid operators, and solar prosumers to manage solar microgrid stations, energy booking slots, reservations, and energy-transfer transactions.

1. Project Information

Module: SE4040 — Enterprise Application Development
Academic Year: 2026 — Year 4 Semester 2
Project Type: Group Assignment
Group Members: 4

Submission

Deadline: 30 September 2026, 11:59 PM

The final submission must contain the complete project source code, report, required diagrams, screenshots, Git repository information, and a video demonstrating the application.

2. System Architecture

The project follows a client-server architecture.

                    ┌───────────────────────┐
                    │      Web Client       │
                    │ React.js + Bootstrap  │
                    └───────────┬───────────┘
                                │
                                │ REST API
                                ▼
                    ┌───────────────────────┐
                    │   Central Web API     │
                    │   ASP.NET Core / C#   │
                    │                       │
                    │ Controllers           │
                    │ Services              │
                    │ Business Logic        │
                    │ Validation            │
                    └───────────┬───────────┘
                                │
                                │ MongoDB Driver
                                ▼
                    ┌───────────────────────┐
                    │     MongoDB Atlas     │
                    │      NoSQL Database   │
                    └───────────────────────┘
                                ▲
                                │
                                │ REST API
                                │
                    ┌───────────┴───────────┐
                    │   Native Android     │
                    │ Kotlin + SQLite      │
                    │ Google Maps           │
                    │ QR Scanner            │
                    └───────────────────────┘

Architecture Rules

The project must follow the FAT Service pattern required by the assignment.

Business logic must reside in the central Web API.

The Web and Android applications are UI/client layers and must communicate with the system through the REST API.

Clients must not directly access MongoDB.

The Android application may use SQLite for the local persistence required by the assignment.

The central service will eventually be deployed on Windows IIS.

The assignment requires a C# Web API with a server-side NoSQL database such as MongoDB.

3. Technology Stack
   Backend
   C#
   ASP.NET Core Web API
   REST API
   MongoDB.Driver
   MongoDB Atlas
   Swagger / OpenAPI
   Windows IIS for deployment
   Web
   React.js
   Bootstrap 5
   JavaScript / HTML / CSS
   REST API communication
   Android
   Native Android
   Kotlin
   Android Studio
   SQLite
   Google Maps API
   QR code scanning
   Development Tools
   Visual Studio Code
   Android Studio
   Git
   GitHub
   MongoDB Atlas
   MongoDB Compass
   Postman
   Swagger
   Important

Do not use:

Flutter
React Native
Xamarin
.NET MAUI
Other cross-platform Android frameworks

The assignment specifically requires a pure native Android application with SQLite.

4. Repository Structure

The project uses a single monorepo.

smart-solar-microgrid/
│
├── README.md
│
├── backend/
│ └── SmartSolarMicrogrid.Api/
│ ├── Controllers/
│ ├── Models/
│ ├── DTOs/
│ ├── Services/
│ ├── Repositories/
│ ├── Data/
│ ├── Program.cs
│ ├── appsettings.json
│ └── SmartSolarMicrogrid.Api.csproj
│
├── web/
│ └── smart-solar-web/
│
├── android/
│ └── SmartSolarMobile/
│
├── database/
│ ├── collections/
│ └── seed/
│
├── docs/
│ ├── diagrams/
│ ├── screenshots/
│ └── api/
│
└── .gitignore 5. Backend Structure

The central API should eventually follow this structure:

SmartSolarMicrogrid.Api/
│
├── Controllers/
│ ├── AuthController.cs
│ ├── UsersController.cs
│ ├── StationsController.cs
│ ├── ReservationsController.cs
│ └── TransactionsController.cs
│
├── Models/
│ ├── User.cs
│ ├── SolarStationInfo.cs
│ ├── EnergyBookingSlot.cs
│ ├── EnergyReservation.cs
│ └── Transaction.cs
│
├── DTOs/
│ ├── LoginRequest.cs
│ ├── RegisterUserRequest.cs
│ └── ...
│
├── Services/
│ ├── AuthService.cs
│ ├── UserService.cs
│ ├── StationService.cs
│ ├── ReservationService.cs
│ └── TransactionService.cs
│
├── Repositories/
│ ├── UserRepository.cs
│ ├── StationRepository.cs
│ ├── ReservationRepository.cs
│ └── TransactionRepository.cs
│
├── Data/
│ └── MongoDbContext.cs
│
├── Program.cs
├── appsettings.json
└── SmartSolarMicrogrid.Api.csproj

This structure is a planned architecture. Individual implementation decisions may be refined during development.

6. Database

The project uses MongoDB Atlas as the server-side NoSQL database.

The assignment marking scheme specifically requires the following four data areas:

User's detail
SolarStationInfo
EnergyBookingSlots
Energy Reservation

All four should contain the required fields and consistent references between related data.

Additional transaction data may be introduced where required by the transaction workflow.

Planned Collections
Users
SolarStationInfo
EnergyBookingSlots
EnergyReservation
Transactions 7. User Roles

The system contains three main user types.

Backoffice

Responsible for administration functions.

Main responsibilities:

User management
Prosumer management
User activation/deactivation
Microgrid station management
Station schedule management

Only Backoffice users should access administrative functions.

Grid Operator

Responsible for operational functions.

Main responsibilities:

View operational reservations
Approve/reject reservations
Monitor energy transactions
Use Android operator mode
Scan transaction QR codes
Verify transactions
Finalize energy transfers
Solar Prosumer

Uses the Android application.

Main responsibilities:

Register account
Login
Manage personal profile
Request account deactivation
View nearby microgrid stations
View available slots
Create reservations
Modify reservations
Cancel reservations
View booking history
Receive approved reservation details
Use transaction QR workflow 8. Group Responsibilities
Member 1 — User & Authentication
Web
Login
Role-based access control
User management
Prosumer management
Activate/deactivate users
Pending user activation
Search/view prosumers
Android
Prosumer registration
Login
Account status
Modify own account
Request account deactivation
API
POST /api/auth/login

POST /api/users/register

GET /api/users

GET /api/users/{nic}

PUT /api/users/{nic}

PUT /api/users/{nic}/activate

PUT /api/users/{nic}/deactivate

GET /api/users/pending

PUT /api/users/{nic}/role
Database
User's detail 9. Member 2 — Microgrid / Station Management
Web
Station list
Create station/node
Edit station/node
Deactivate station/node
Manage GPS/location
Manage capacity
Manage battery slots
Manage station schedules
Android
Nearby grid nodes
Google Maps
Station details
Station availability
Available slots
API
GET /api/stations

GET /api/stations/{id}

POST /api/stations

PUT /api/stations/{id}

PUT /api/stations/{id}/deactivate

GET /api/stations/nearby

GET /api/stations/{id}/availability

GET /api/stations/{id}/schedule

PUT /api/stations/{id}/schedule
Database
SolarStationInfo 10. Member 3 — Reservation Management
Web
Reservation list
Current/upcoming reservations
Reservation history
Search and filter reservations
Reservations dashboard
View reservation details
Android
View available slots
Create reservation
Reservation summary
Update reservation
Cancel reservation
View current/pending bookings
View booking history
Search/filter reservations
API
GET /api/slots

GET /api/slots/{stationId}

GET /api/slots/available

POST /api/reservations

GET /api/reservations/{id}

GET /api/reservations/user/{nic}

PUT /api/reservations/{id}

DELETE /api/reservations/{id}

GET /api/reservations/pending

GET /api/reservations/current

GET /api/reservations/history

GET /api/reservations/search
Database
EnergyBookingSlots
EnergyReservation
Business Rules

Reservations must:

Be scheduled within 7 days.
Respect the required notice period for modifications.
Respect the required notice period for cancellations.

The assignment specifically states that reservations must be scheduled within 7 days and updates/cancellations require at least 12 hours' notice.

11. Member 4 — Operator, Approval & Energy Transaction
    Web
    Pending reservation approval
    Approve reservation
    Reject reservation
    Transaction/transfer list
    Transaction details
    Operational history
    Android
    Operator Mode
    QR scanning
    Server verification
    Transaction details after scanning
    Finalize energy transfer
    API
    GET /api/reservations/pending

PUT /api/reservations/{id}/approve

PUT /api/reservations/{id}/reject

POST /api/transactions/verify

GET /api/transactions/{id}

POST /api/transactions/{id}/complete

GET /api/transactions/pending

GET /api/transactions/completed
Important API Ownership

GET /api/reservations/pending currently appears in both Member 3 and Member 4 plans.

This endpoint ownership must be agreed by the team before implementation.

Recommended responsibility:

Member 3:
GET /api/reservations/pending

Member 4:
PUT /api/reservations/{id}/approve
PUT /api/reservations/{id}/reject 12. Core Business Workflow
Prosumer Reservation
Prosumer
│
▼
Android App
│
▼
GET available slots
│
▼
Central API
│
▼
MongoDB
│
▼
Available slots
│
▼
Prosumer selects slot
│
▼
POST reservation
│
▼
API validates business rules
│
▼
Reservation stored
Reservation Approval
Reservation
│
▼
Grid Operator / Backoffice
│
▼
Pending Reservations
│
├── Approve
│
└── Reject

Once approved, the prosumer can proceed with the transaction workflow.

13. QR Transaction Workflow

The planned transaction flow is:

Approved Reservation
│
▼
Prosumer Android App
│
▼
Generate Transaction QR
│
▼
Grid Operator scans QR
│
▼
Central API verifies transaction
│
▼
Transaction details returned
│
▼
Operator confirms energy transfer
│
▼
Central API finalizes transaction
│
▼
Transaction marked completed

The assignment requires the operator to scan the prosumer's transaction QR code, verify it against the server, and finalize the job.

14. Android Local Storage

SQLite is required for the native Android application.

SQLite should be used for appropriate local persistence such as:

Login/reference information
Required local user information
Reference data

However, MongoDB must remain server-side.

The Android client must not connect directly to MongoDB.

The assignment specifically assesses SQLite local persistence and requires clients to communicate through the central Web API.

15. Google Maps

The Android application should use Google Maps API to display nearby solar microgrid nodes.

Station locations should come from the API/database rather than hard-coded station locations.

Each station should provide appropriate location information such as:

Latitude
Longitude
Station details
Capacity
Availability

The assignment requires nearby grid nodes to be plotted using their stored latitude and longitude.

16. API Communication Rule

Both clients communicate with the central API.

Web ──────────────► REST API ◄────────────── Android
│
▼
MongoDB

Do not implement:

Web ───────────────► MongoDB
Android ────────────► MongoDB

Business rules must be implemented centrally in the API rather than duplicated in the clients.

17. Authentication

Authentication should be handled by the central API.

The API determines the user's role and returns the appropriate authentication result.

The clients use the result to display the appropriate UI.

Example roles:

BACKOFFICE
GRID_OPERATOR
PROSUMER

Role-specific authorization should be enforced by the API rather than relying only on hiding UI elements.

22. Error Handling

The API should return appropriate HTTP responses.

Examples:

200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error

Clients should display meaningful error messages rather than exposing raw server errors.

25. Deployment

The central Web API is intended to be deployed on:

Windows IIS Server

Target architecture:

Web Browser
│
▼
Web Application
│
▼
Hosted C# Web API
│
▼
MongoDB Atlas

and:

Android Application
│
▼
Hosted C# Web API
│
▼
MongoDB Atlas

Both clients must be able to reach the deployed service.
