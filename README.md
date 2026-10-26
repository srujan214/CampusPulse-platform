# CampusPulse ⚡

> **QR-Powered Smart Campus Issue Management and Intelligent Prioritization System**  
> Complete Full-Stack Web Application for College Project Exhibition.

---

## 🚀 Key Features

1. **Futuristic Cyber-Navy UI**:
   - Matches the design specification: Dark navy background (`#070b16`), electric cyan (`#00f2fe`), neon teal, violet highlights, and rounded glassmorphism cards.
   - 9 complete screens: Welcome/Login, Student Dashboard, Report Issue, Complaint Details & Tracking, Staff Dashboard, Staff Management, Location QR Codes, Help & About, and Mobile Responsive Layout.

2. **File-Based H2 Database Persistence**:
   - Zero in-memory loss: Database stored locally at `./data/campuspulse.mv.db`.
   - Verified across multiple independent application restarts.

3. **Pure Java QR Code Generation**:
   - Implemented using Google ZXing (`com.google.zxing:core` & `javase`).
   - Zero Python dependencies or external microservices.
   - Dedicated QR Management screen to generate, preview, and print QR codes for all campus locations.
   - Automatic form preselection upon scanning (e.g., `?location=Lab-2`).

4. **Intelligent Prioritization & Local Fallback Engine**:
   - Computes an explainable 0–100 priority score with dynamic factor breakdown.
   - Recognizes safety hazard keywords (`fire`, `sparks`, `smoke`, `shock`, `leak`, `flooding`, `injury`, `chemical spill`).
   - Optional Claude/Gemini AI integration with automatic local fallback. If offline or no API key is present, the app continues without errors.

5. **Role-Based Security & Permissions**:
   - Spring Security with BCrypt password hashing and JWT stateless tokens.
   - Strict data ownership: Students can only view their own private reports; only staff can update statuses.

6. **Interactive Real-Time Analytics**:
   - Powered by Chart.js (Donut chart for status breakdown, Bar chart for categories).
   - Real-time counters: Total, Submitted, Open, In Progress, Resolved, Overdue.

---

## 🛠️ Development Environment

- **Operating System**: Windows 10/11
- **Java Version**: Oracle JDK 25.0.4.1 (Bytecode compiled with `--release 21` compatibility)
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Apache Maven 3.9.16 / Maven Wrapper (`mvnw`)
- **Database**: H2 (File-based storage in `./backend/data/campuspulse.mv.db`)
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, Chart.js

---

## ⚡ Quick Start Guide (Windows)

### 1. Verify Java Installation
Open PowerShell and verify:
```powershell
java -version
```
Ensure JDK 25 is active.

### 2. Build the Application
From the project root:
```powershell
.\mvnw clean package -DskipTests
```
Or navigate to `backend` and run:
```powershell
cd backend
..\mvnw clean package -DskipTests
```

### 3. Start the Backend Server
```powershell
cd backend
mvn spring-boot:run
```
Or using Maven Wrapper:
```powershell
cd backend
..\mvnw spring-boot:run
```

The server will start on: **`http://localhost:8080`**

### 4. Open the Web Application
Open your web browser and go to:
👉 **`http://localhost:8080`**

---

## 🔑 Demo Demonstration Accounts

Pre-configured accounts are ready to use immediately:

| Role | Name | Email / ID | Password | Quick Fill Pill |
| :--- | :--- | :--- | :--- | :--- |
| **Staff** | Rahul Sharma | `rahul.staff@campuspulse.edu` (or `STF-201`) | `Password@123` | Click **"Rahul (Staff)"** on login |
| **Student** | Alex Rivera | `alex.student@campuspulse.edu` (or `STU-2024-001`) | `Password@123` | Click **"Alex (Student)"** on login |
| **Student** | Priya Sharma | `priya.sharma@campuspulse.edu` (or `STU-2024-002`) | `Password@123` | Enter credentials |

---

## 📱 QR Code Testing Workflow

1. Log in as **Staff** (`rahul.staff@campuspulse.edu`).
2. Go to the **QR Codes** screen from the sidebar.
3. You will see generated QR codes for **Lab-1, Lab-2, Library, Block-A, Block-B, Classroom, Canteen, Washroom**.
4. Click **"Test Scan"** on any card (e.g. `Lab-2`) or scan the QR code with your phone camera.
5. CampusPulse opens the **Report Issue** form with **"Lab-2"** preselected automatically.
6. Submit an issue: the location is saved to the complaint record.

---

## 🤖 AI & Offline Fallback Configuration

CampusPulse works 100% out-of-the-box in local fallback mode. To optionally activate Claude or Gemini AI:

### For Gemini:
```powershell
$env:AI_PROVIDER="gemini"
$env:GEMINI_API_KEY="your-gemini-api-key"
```

### For Claude:
```powershell
$env:AI_PROVIDER="claude"
$env:CLAUDE_API_KEY="your-claude-api-key"
```

If the API key is absent, network fails, or timeout occurs, the **deterministic local scoring engine** scores the complaint in Java without losing any student submission.

---

## 💾 H2 Database File Location & Backup

- **Database File**: `backend/data/campuspulse.mv.db`
- **H2 Web Console**: Accessible at `http://localhost:8080/h2-console`
  - **JDBC URL**: `jdbc:h2:file:./data/campuspulse;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`
  - **User**: `sa`
  - **Password**: *(leave empty)*
- **Backup Instructions**:
  To back up the database, simply copy `backend/data/campuspulse.mv.db` while the server is stopped.

---

## 📁 Project Architecture

```
CampusPulse2/
├── backend/                       # Spring Boot Application
│   ├── pom.xml                    # Maven configuration
│   ├── data/                      # Persistent H2 file storage
│   ├── src/main/java/com/campuspulse/
│   │   ├── ai/                    # AiService & FallbackScoringEngine
│   │   ├── config/                # DataInitializer & WebMvcConfig
│   │   ├── controller/            # REST API endpoints
│   │   ├── dto/                   # Request/Response DTOs
│   │   ├── entity/                # JPA entities (Complaint, User, StatusHistory, etc.)
│   │   ├── exception/             # GlobalExceptionHandler
│   │   ├── qr/                    # ZXing Java QR code generator
│   │   ├── repository/            # Spring Data JPA repositories
│   │   ├── security/              # JWT filter, UserDetails, SecurityConfig
│   │   └── service/               # Business logic services
│   └── src/main/resources/
│       ├── application.properties # H2, JWT, and upload settings
│       └── static/                # Synced frontend assets
├── frontend/                      # Standalone frontend files
│   ├── index.html                 # 9 application screens
│   ├── css/styles.css             # Cyber-navy glassmorphism design system
│   └── js/app.js                  # Complete application frontend logic
├── docs/                          # Comprehensive project documentation
│   ├── ARCHITECTURE.md            # System architecture & diagrams
│   ├── DATABASE_DESIGN.md         # Schema design & ER diagram
│   ├── TESTING_GUIDE.md           # Step-by-step test verification
│   └── DEMO_SCRIPT.md             # 3-5 minute exhibition walkthrough script
├── BUILD_STATUS.md                # Verified testing & build status
└── README.md                      # Setup & user guide
```
