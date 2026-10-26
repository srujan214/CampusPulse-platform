# CampusPulse — 3–5 Minute Exhibition Demonstration Script

Use this script during your college project exhibition to deliver a crisp presentation to professors and evaluators.

---

## ⏱️ Step-by-Step Presentation Timeline

### Minute 0:00 – 0:45 | Introduction & Problem Statement
1. **Show Screen 1 (Welcome Screen)** on the laptop projector or screen.
2. **Opening Statement**:
   > *"Good morning professors. Traditional college maintenance relies on paper complaint registers, untracked emails, or chaotic WhatsApp messages. When an issue occurs in a laboratory or classroom—like an exposed electrical wire or a broken projector—staff don't know the urgency, and students have zero visibility into when it will be fixed."*
3. **Present the Solution**:
   > *"We built **CampusPulse**, a smart campus issue management system that combines pure Java QR code tags at campus locations, explainable intelligent prioritization, file-based database persistence, and an interactive tracking workflow."*

---

### Minute 0:45 – 1:45 | QR Code Scanning & Student Issue Reporting
1. Click **"Alex (Student)"** &rarr; **Sign In**.
2. **Simulate or show the QR code scan**:
   > *"Imagine a student walks into **Lab-2** and spots electrical sparks or a broken projector. They don't need any special app—they just point their phone's native camera at the printed QR code on the wall."*
3. Open `http://localhost:8080/index.html?location=Lab-2#report`.
4. Point out the glowing alert:
   > *"Notice how the application automatically recognized the location as **Lab-2** without the student having to search for it."*
5. Fill in:
   - **Title**: `Electrical sparks from power strip`
   - **Category**: `Electrical`
   - **Severity**: `High`
   - **Description**: `Visible sparks emitting near workbench 4.`
6. Point to the live preview:
   > *"Notice the instant safety hazard detection warning before submission."*
7. Click **Submit Issue**.
8. Show the created Ticket ID (e.g. `CP-1008`) and the tracking screen with the **Submitted** step in the vertical status stepper.

---

### Minute 1:45 – 2:45 | Staff Dashboard, Intelligent Prioritization & Status Update
1. Sign out and click **"Rahul (Staff)"** &rarr; **Sign In**.
2. **Show Screen 5 (Staff Dashboard)**:
   > *"This is the Staff Management Center. Notice the real-time counters and live Chart.js analytics: the Donut chart shows issues by status, and the Bar chart shows issues by category."*
3. Open the newly submitted complaint by clicking **Manage**.
4. **Highlight Screen 6 (Staff Management & Explainable Score)**:
   > *"Look at the Priority Score gauge: **85 / 100 [ Critical ]**. Rather than a black-box AI number, CampusPulse provides an **explainable breakdown**: High severity rating (+30 pts), Hazard keyword 'sparks' detected (+35 pts), and Laboratory technical zone penalty (+12 pts)."*
   > *"If an internet connection to Gemini or Claude is unavailable, our **deterministic local Java fallback engine** ensures issues are scored accurately without failing."*
5. Change status to `IN_PROGRESS`, add remark: `Electrician dispatched with replacement breaker`, and click **Update Status**.
6. Show that the **Status History** updates in real-time with Rahul Sharma's name and timestamp.

---

### Minute 2:45 – 3:30 | Student Verification & Multi-Device Mobile View
1. Sign back in as student Alex and open the ticket.
2. Show that the vertical timeline has now progressed to **In Progress** with the staff remark visible.
3. Switch browser to **Mobile Emulation** (`Ctrl+Shift+M` or phone view):
   > *"Students can use this smoothly on any phone screen width—360px, 390px, or 430px—with a thumb-friendly bottom navigation bar."*

---

### Minute 3:30 – 4:00 | Persistence Proof & Conclusion
1. **Explain Database Persistence**:
   > *"Unlike basic prototypes that use in-memory databases and lose everything on restart, CampusPulse uses a genuine **file-based H2 database** (`campuspulse.mv.db`). Every single ticket, timeline transition, and remark survives complete backend shutdowns."*
2. **Show the QR Management Screen**:
   > *"Staff can also view, download, and print location QR codes for any room on campus with a single click."*
3. **Closing Statement**:
   > *"CampusPulse is 100% genuine, fully working, and exhibition-ready. Thank you!"*
