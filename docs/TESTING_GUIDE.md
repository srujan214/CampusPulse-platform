# CampusPulse — Testing & Verification Guide

This guide details how to verify all features of the application locally.

---

## 1. Running the Automated Test Suite

From PowerShell in the `backend/` directory:
```powershell
mvn test
```

### What is tested:
1. **Spring Boot Context & Initial Seed Data**: Verifies database connects, entities map correctly, and demonstration records load.
2. **Authentication & Password Hashing**: Verifies student and staff logins with BCrypt matching and JWT creation.
3. **Deterministic Local Fallback Engine**: Tests keyword identification (`sparks`, `water leak`), score calculations, and department resolution.
4. **Complaint Submission & Status Timeline**: Verifies ticket ID generation, initial timeline recording, and status transitions.
5. **Pure Java ZXing QR Generation**: Verifies valid QR PNG byte arrays and base64 data URLs.

---

## 2. Running the 10-Step Persistence Test Across Context Restarts

To verify that the file-based H2 database genuinely preserves records across backend restarts:
```powershell
mvn test -Dtest=DatabasePersistenceTest
```

### Sequence verified:
1. **Context 1**: Starts up, logs in as student Alex, submits a new complaint, records ticket ID `CP-XXXX`.
2. **Context 1**: Closes and shuts down completely.
3. **Context 2**: Starts from scratch reading the same disk file `./data/campuspulse.mv.db`, confirms `CP-XXXX` still exists, and updates status to `IN_PROGRESS` as staff Rahul.
4. **Context 2**: Closes and shuts down completely.
5. **Context 3**: Starts from scratch, confirms `CP-XXXX` is still `IN_PROGRESS`, and asserts the complete status history is intact.

---

## 3. Manual End-to-End Verification Steps

### Test 1: Student Report Workflow
1. Open `http://localhost:8080`.
2. Click **"Alex (Student)"** to quick-fill credentials (`alex.student@campuspulse.edu` / `Password@123`), then click **Sign In**.
3. You will land on the **Student Dashboard**.
4. Click **"+ Report New"** or use the sidebar link **Report Issue**.
5. Enter:
   - **Title**: `Projector flashing red error in Lab-2`
   - **Category**: `Classroom Equipment`
   - **Location**: `Lab-2`
   - **Severity**: `High`
   - **Description**: `Power lamp fails and warning buzzer sounds.`
6. Observe the real-time AI hint preview under the description.
7. Click **Submit Issue**.
8. Notice the instant success toast with the assigned ticket ID (e.g. `CP-1008`), and the tracking view opens.
9. Verify the vertical stepper shows **Submitted** with your timestamp.

### Test 2: Staff Status Update & Remarks Workflow
1. Sign out and click **"Rahul (Staff)"** on the login screen (`rahul.staff@campuspulse.edu` / `Password@123`).
2. You will land on the **Staff Dashboard**.
3. View the 6 metric cards and Chart.js **Complaints by Status** (donut) and **Complaints by Category** (bar).
4. Locate the newly submitted complaint in the table.
5. Click **Manage**.
6. View the **Priority Score Box** on the right (`78 / 100 [ High ]`) with the "Why this score?" factor breakdown.
7. Under **Update Complaint Status**, select `IN_PROGRESS`.
8. In the remarks box, type: `Technician assigned with replacement lamp.`
9. Click **Update Status**.
10. Observe that the status history table updates immediately with Rahul Sharma's name and the new status.

### Test 3: QR Code Location Preselection
1. Go to the **QR Codes** screen from the staff sidebar.
2. Click **Test Scan** on **"Lab-1"** or navigate directly in your browser to:
   `http://localhost:8080/index.html?location=Lab-1#report`
3. Notice the cyan alert box: **"QR Code Scanned: Location automatically selected as Lab-1"**.
4. The Location dropdown has **"Lab-1"** selected automatically!

### Test 4: Mobile View Verification
1. Press `F12` in Chrome/Edge and click the **Device Toggle** button (or press `Ctrl+Shift+M`).
2. Select **iPhone 14** (390px) or **Samsung Galaxy S20** (360px).
3. Verify that:
   - The desktop sidebar cleanly transitions to the thumb-friendly **Mobile Bottom Navigation Bar**.
   - Metric cards adapt into a 2-column stacked grid.
   - All forms, touch buttons, and tables remain fully legible without horizontal overflow.
