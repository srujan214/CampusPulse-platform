/**
 * CampusPulse — Smart Campus Issue Management System
 * Complete Frontend Application Logic (Vanilla JavaScript)
 */

// Application State
const state = {
  token: localStorage.getItem('campuspulse_token') || null,
  user: JSON.parse(localStorage.getItem('campuspulse_user') || 'null'),
  locations: [],
  currentRoleTab: 'STUDENT',
  currentView: 'login',
  activeTicketId: null,
  selectedPhotoFile: null,
  chartStatus: null,
  chartCategory: null
};

// API Base URL (Relative path works seamlessly with Spring Boot)
const API_BASE = '';

// =============================================================================
// Initialization on Page Load
// =============================================================================
document.addEventListener('DOMContentLoaded', async () => {
  setupPhotoDropzone();
  await loadLocations();

  // Check URL parameters for QR scan location preselection
  const urlParams = new URLSearchParams(window.location.search);
  const scannedLocation = urlParams.get('location');

  if (state.token && state.user) {
    setupAppForUser(state.user);
    if (scannedLocation) {
      navigateTo('report', scannedLocation);
    } else {
      if (state.user.role === 'ROLE_STAFF' || state.user.role === 'ROLE_ADMIN') {
        navigateTo('staff-dashboard');
      } else {
        navigateTo('student-dashboard');
      }
    }
  } else {
    // If not logged in but scanned QR code, save preselected location for post-login
    if (scannedLocation) {
      sessionStorage.setItem('pending_qr_location', scannedLocation);
      showNotification(`Scanned location: ${scannedLocation}. Please sign in to file your report.`, 'info');
    }
    showLoginScreen();
  }
});

// =============================================================================
// Authentication & Session
// =============================================================================
function setLoginRole(role) {
  state.currentRoleTab = role;
  document.getElementById('tab-role-student').classList.toggle('active', role === 'STUDENT');
  document.getElementById('tab-role-staff').classList.toggle('active', role === 'STAFF');

  const label = document.getElementById('label-email-id');
  const input = document.getElementById('login-email-id');
  if (role === 'STAFF') {
    label.innerText = 'Staff Email / Employee ID';
    input.placeholder = 'e.g. rahul.staff@campuspulse.edu or STF-201';
  } else {
    label.innerText = 'Student Email / Roll ID';
    input.placeholder = 'e.g. alex.student@campuspulse.edu or STU-2024-001';
  }
}

function fillDemoCredentials(type) {
  if (type === 'student') {
    setLoginRole('STUDENT');
    document.getElementById('login-email-id').value = 'alex.student@campuspulse.edu';
    document.getElementById('login-password').value = 'Password@123';
  } else {
    setLoginRole('STAFF');
    document.getElementById('login-email-id').value = 'rahul.staff@campuspulse.edu';
    document.getElementById('login-password').value = 'Password@123';
  }
}

function showRegisterBox(show) {
  document.getElementById('auth-login-box').style.display = show ? 'none' : 'block';
  document.getElementById('auth-register-box').style.display = show ? 'block' : 'none';
  hideError('login-error-msg');
  hideError('reg-error-msg');
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const emailOrId = document.getElementById('login-email-id').value.trim();
  const password = document.getElementById('login-password').value;
  const btn = document.getElementById('btn-login-submit');

  hideError('login-error-msg');
  btn.disabled = true;
  btn.innerText = 'Authenticating...';

  try {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ emailOrId, password })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Invalid credentials');
    }

    // Save session
    state.token = data.token;
    state.user = {
      id: data.id,
      email: data.email,
      fullName: data.fullName,
      studentOrStaffId: data.studentOrStaffId,
      role: data.role,
      department: data.department
    };

    localStorage.setItem('campuspulse_token', state.token);
    localStorage.setItem('campuspulse_user', JSON.stringify(state.user));

    setupAppForUser(state.user);

    // Check if there was a pending QR scan location
    const pendingLoc = sessionStorage.getItem('pending_qr_location');
    if (pendingLoc) {
      sessionStorage.removeItem('pending_qr_location');
      navigateTo('report', pendingLoc);
    } else {
      if (state.user.role === 'ROLE_STAFF' || state.user.role === 'ROLE_ADMIN') {
        navigateTo('staff-dashboard');
      } else {
        navigateTo('student-dashboard');
      }
    }
  } catch (err) {
    showError('login-error-msg', err.message);
  } finally {
    btn.disabled = false;
    btn.innerText = 'Sign In';
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();
  const fullName = document.getElementById('reg-name').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const studentId = document.getElementById('reg-studentid').value.trim();
  const department = document.getElementById('reg-department').value.trim();
  const password = document.getElementById('reg-password').value;
  const btn = document.getElementById('btn-reg-submit');

  hideError('reg-error-msg');
  btn.disabled = true;
  btn.innerText = 'Creating account...';

  try {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, studentId, department, password })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Registration failed');
    }

    state.token = data.token;
    state.user = {
      id: data.id,
      email: data.email,
      fullName: data.fullName,
      studentOrStaffId: data.studentOrStaffId,
      role: data.role,
      department: data.department
    };

    localStorage.setItem('campuspulse_token', state.token);
    localStorage.setItem('campuspulse_user', JSON.stringify(state.user));

    setupAppForUser(state.user);
    showNotification('Account created successfully! Welcome to CampusPulse.', 'success');
    navigateTo('student-dashboard');
  } catch (err) {
    showError('reg-error-msg', err.message);
  } finally {
    btn.disabled = false;
    btn.innerText = 'Create Account';
  }
}

function handleLogout() {
  localStorage.removeItem('campuspulse_token');
  localStorage.removeItem('campuspulse_user');
  state.token = null;
  state.user = null;
  showLoginScreen();
  showNotification('You have signed out successfully.', 'info');
}

function showLoginScreen() {
  document.getElementById('screen-login').style.display = 'flex';
  document.getElementById('app-layout').style.display = 'none';
  document.getElementById('mobile-bottom-nav').style.display = 'none';
}

function setupAppForUser(user) {
  document.getElementById('screen-login').style.display = 'none';
  document.getElementById('app-layout').style.display = 'flex';

  const isMobile = window.innerWidth <= 992;
  document.getElementById('mobile-bottom-nav').style.display = isMobile ? 'flex' : 'none';

  // Set Top Header details
  document.getElementById('header-user-name').innerText = user.fullName;
  document.getElementById('header-avatar').innerText = user.fullName.charAt(0).toUpperCase();

  const isStaff = user.role === 'ROLE_STAFF' || user.role === 'ROLE_ADMIN';
  document.getElementById('header-user-role').innerText = isStaff ? 'Staff' : 'Student';
  document.getElementById('header-greeting').innerText = isStaff ? `Hello, ${user.fullName.split(' ')[0]}!` : `Good morning, ${user.fullName.split(' ')[0]}!`;
  document.getElementById('header-subtext').innerText = isStaff ? "Here's the overall campus issue summary." : "Here's what's happening with your campus issues.";

  // Build Sidebar & Mobile Navigation
  buildNavigation(isStaff);
}

// =============================================================================
// Navigation & Views
// =============================================================================
function buildNavigation(isStaff) {
  const sidebarNav = document.getElementById('sidebar-nav');
  const mobileNav = document.getElementById('mobile-bottom-nav');

  let items = [];
  if (isStaff) {
    items = [
      { id: 'staff-dashboard', label: 'Home', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
      { id: 'qr-codes', label: 'QR Codes', icon: 'M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z' },
      { id: 'help', label: 'Help & About', icon: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' }
    ];
  } else {
    items = [
      { id: 'student-dashboard', label: 'Home', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
      { id: 'report', label: 'Report Issue', icon: 'M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z' },
      { id: 'my-complaints', label: 'My Complaints', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01' },
      { id: 'qr-codes', label: 'Location QRs', icon: 'M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z' },
      { id: 'help', label: 'Help', icon: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' }
    ];
  }

  // Render Sidebar Nav
  sidebarNav.innerHTML = items.map(item => `
    <li>
      <a href="javascript:void(0)" class="nav-item ${item.id === state.currentView ? 'active' : ''}" id="nav-${item.id}" onclick="navigateTo('${item.id}')">
        <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="${item.icon}" /></svg>
        <span>${item.label}</span>
      </a>
    </li>
  `).join('');

  // Render Mobile Bottom Nav
  mobileNav.innerHTML = items.map(item => `
    <button class="mobile-nav-btn ${item.id === state.currentView ? 'active' : ''}" id="mobile-nav-${item.id}" onclick="navigateTo('${item.id}')">
      <svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="${item.icon}" /></svg>
      <span>${item.label}</span>
    </button>
  `).join('');
}

function navigateTo(viewId, extraParam = null) {
  state.currentView = viewId;

  // Hide all views
  document.querySelectorAll('.app-view').forEach(el => el.style.display = 'none');

  // Update active navigation classes
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  document.querySelectorAll('.mobile-nav-btn').forEach(el => el.classList.remove('active'));

  const activeNavItem = document.getElementById(`nav-${viewId}`);
  if (activeNavItem) activeNavItem.classList.add('active');
  const activeMobileItem = document.getElementById(`mobile-nav-${viewId}`);
  if (activeMobileItem) activeMobileItem.classList.add('active');

  // Show specific view
  if (viewId === 'student-dashboard') {
    document.getElementById('view-student-dashboard').style.display = 'block';
    loadStudentDashboard();
  } else if (viewId === 'staff-dashboard') {
    document.getElementById('view-staff-dashboard').style.display = 'block';
    loadStaffDashboard();
  } else if (viewId === 'report') {
    document.getElementById('view-report-issue').style.display = 'block';
    setupReportForm(extraParam);
  } else if (viewId === 'my-complaints') {
    document.getElementById('view-my-complaints').style.display = 'block';
    loadStudentAllComplaints();
  } else if (viewId === 'complaint-details') {
    document.getElementById('view-complaint-details').style.display = 'block';
    if (extraParam) {
      loadComplaintDetails(extraParam);
    }
  } else if (viewId === 'staff-management') {
    document.getElementById('view-staff-management').style.display = 'block';
    if (extraParam) {
      loadStaffManagement(extraParam);
    }
  } else if (viewId === 'qr-codes') {
    document.getElementById('view-qr-management').style.display = 'block';
    loadQrCodesView();
  } else if (viewId === 'help') {
    document.getElementById('view-help').style.display = 'block';
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// =============================================================================
// Locations & QR Preselection
// =============================================================================
async function loadLocations() {
  try {
    const res = await fetch(`${API_BASE}/api/locations`);
    if (res.ok) {
      state.locations = await res.json();
      populateLocationDropdown();
    }
  } catch (e) {
    console.warn('Could not fetch locations:', e);
  }
}

function populateLocationDropdown() {
  const select = document.getElementById('report-location');
  if (!select) return;

  const currentVal = select.value;
  select.innerHTML = '<option value="">Select location</option>' + 
    state.locations.map(loc => `<option value="${loc.name}">${loc.name} (${loc.building || 'Campus'})</option>`).join('');

  if (currentVal) select.value = currentVal;
}

function setupReportForm(preselectedLocation) {
  const banner = document.getElementById('qr-autofill-banner');
  const locSelect = document.getElementById('report-location');

  populateLocationDropdown();

  if (preselectedLocation) {
    banner.style.display = 'flex';
    document.getElementById('qr-autofill-location-name').innerText = preselectedLocation;

    // Check if location exists in dropdown, else add as option
    let found = false;
    for (let i = 0; i < locSelect.options.length; i++) {
      if (locSelect.options[i].value.toLowerCase() === preselectedLocation.toLowerCase()) {
        locSelect.selectedIndex = i;
        found = true;
        break;
      }
    }
    if (!found) {
      const opt = document.createElement('option');
      opt.value = preselectedLocation;
      opt.innerText = preselectedLocation;
      opt.selected = true;
      locSelect.appendChild(opt);
    }
  } else {
    banner.style.display = 'none';
  }
}

// =============================================================================
// Screen 2: Student Dashboard
// =============================================================================
async function loadStudentDashboard() {
  try {
    // 1. Fetch Student Analytics
    const statsRes = await authFetch(`${API_BASE}/api/analytics/student`);
    if (statsRes.ok) {
      const stats = await statsRes.json();
      document.getElementById('stat-student-total').innerText = stats.total || 0;
      document.getElementById('stat-student-open').innerText = stats.open || 0;
      document.getElementById('stat-student-in-progress').innerText = stats.inProgress || 0;
      document.getElementById('stat-student-resolved').innerText = stats.resolved || 0;
    }

    // 2. Fetch Recent Complaints
    const listRes = await authFetch(`${API_BASE}/api/complaints/my`);
    if (listRes.ok) {
      const complaints = await listRes.json();
      renderComplaintTable(complaints.slice(0, 5), 'student-recent-complaints-tbody');
    }
  } catch (err) {
    console.error('Failed to load student dashboard:', err);
  }
}

async function loadStudentAllComplaints() {
  try {
    const res = await authFetch(`${API_BASE}/api/complaints/my`);
    if (res.ok) {
      const complaints = await res.json();
      renderComplaintTable(complaints, 'student-all-complaints-tbody');
    }
  } catch (err) {
    console.error('Failed to load complaints:', err);
  }
}

function renderComplaintTable(list, tbodyId) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 32px; color: var(--text-dim);">No complaints reported yet. Click "+ Report New" to report an issue.</td></tr>`;
    return;
  }

  tbody.innerHTML = list.map(c => `
    <tr>
      <td><strong style="color:#fff;">${c.ticketId}</strong></td>
      <td>
        <div style="font-weight: 600; color: #fff;">${escapeHtml(c.title)}</div>
        <div style="font-size: 0.75rem; color: var(--text-dim);">${c.categoryDisplayName || c.category}</div>
      </td>
      <td>${escapeHtml(c.location)}</td>
      <td>${renderPriorityBadge(c.priorityLevel, c.priorityScore)}</td>
      <td>${renderStatusBadge(c.status)}</td>
      <td style="color: var(--text-muted); font-size: 0.825rem;">${formatDate(c.createdAt)}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="navigateTo('complaint-details', '${c.ticketId}')">
          View Details
        </button>
      </td>
    </tr>
  `).join('');
}

// =============================================================================
// Screen 3: Report New Issue
// =============================================================================
function selectSeverity(level) {
  document.getElementById('report-severity').value = level;
  document.querySelectorAll('.severity-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-severity') === level);
  });
  handleIssueTextInput();
}

function handleIssueTextInput() {
  const title = (document.getElementById('report-title').value || '').toLowerCase();
  const desc = (document.getElementById('report-description').value || '').toLowerCase();
  const combined = title + ' ' + desc;

  const hazardKeywords = ['fire', 'sparks', 'smoke', 'electric shock', 'short circuit', 'exposed wire', 'water leak', 'flooding', 'gas leak', 'explosion', 'hazard'];
  let detected = null;

  for (const kw of hazardKeywords) {
    if (combined.includes(kw)) {
      detected = kw;
      break;
    }
  }

  const hintBox = document.getElementById('ai-realtime-hint');
  const hintText = document.getElementById('ai-realtime-text');

  if (detected) {
    hintBox.style.display = 'block';
    hintBox.style.background = 'rgba(239, 68, 68, 0.15)';
    hintBox.style.borderColor = '#ef4444';
    hintText.innerHTML = `⚠️ <strong>Safety Hazard Indicator:</strong> Keyword "<em>${detected}</em>" detected. CampusPulse deterministic prioritization will assign high risk rating and notify facilities.`;
  } else if (combined.length > 15) {
    hintBox.style.display = 'block';
    hintBox.style.background = 'rgba(0, 242, 254, 0.08)';
    hintBox.style.borderColor = 'var(--border-glow)';
    hintText.innerHTML = `🤖 <strong>AI Prioritization Active:</strong> Will evaluate severity, category impact, and facility risk upon submission.`;
  } else {
    hintBox.style.display = 'none';
  }
}

function setupPhotoDropzone() {
  const dropzone = document.getElementById('report-dropzone');
  if (!dropzone) return;

  ['dragenter', 'dragover'].forEach(eventName => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    }, false);
  });

  ['dragleave', 'drop'].forEach(eventName => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
    }, false);
  });

  dropzone.addEventListener('drop', (e) => {
    const dt = e.dataTransfer;
    const files = dt.files;
    if (files && files.length > 0) {
      processPhotoFile(files[0]);
    }
  }, false);
}

function handlePhotoSelect(e) {
  if (e.target.files && e.target.files[0]) {
    processPhotoFile(e.target.files[0]);
  }
}

function processPhotoFile(file) {
  if (file.size > 5 * 1024 * 1024) {
    alert('File size exceeds 5MB limit. Please choose a smaller photo.');
    return;
  }
  state.selectedPhotoFile = file;

  const previewBox = document.getElementById('photo-preview-box');
  const previewImg = document.getElementById('photo-preview-img');
  const previewName = document.getElementById('photo-preview-name');

  const reader = new FileReader();
  reader.onload = (e) => {
    previewImg.src = e.target.result;
    previewName.innerText = file.name;
    previewBox.style.display = 'flex';
  };
  reader.readAsDataURL(file);
}

function clearPhotoSelection() {
  state.selectedPhotoFile = null;
  document.getElementById('report-photo-input').value = '';
  document.getElementById('photo-preview-box').style.display = 'none';
}

async function handleReportSubmit(e) {
  e.preventDefault();
  const title = document.getElementById('report-title').value.trim();
  const category = document.getElementById('report-category').value;
  const location = document.getElementById('report-location').value;
  const severity = document.getElementById('report-severity').value;
  const description = document.getElementById('report-description').value.trim();
  const btn = document.getElementById('btn-submit-issue');

  if (!title || !category || !location || !description) {
    alert('Please fill in all required fields.');
    return;
  }

  btn.disabled = true;
  btn.innerHTML = '<span>Submitting & Prioritizing...</span>';

  try {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('category', category);
    formData.append('location', location);
    formData.append('severity', severity);
    formData.append('description', description);
    if (state.selectedPhotoFile) {
      formData.append('photo', state.selectedPhotoFile);
    }

    const res = await fetch(`${API_BASE}/api/complaints/submit`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${state.token}`
      },
      body: formData
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Submission failed');
    }

    // Reset Form
    document.getElementById('form-report-issue').reset();
    clearPhotoSelection();
    document.getElementById('ai-realtime-hint').style.display = 'none';

    showNotification(`Complaint registered successfully! Assigned Ticket ID: ${data.ticketId}`, 'success');
    navigateTo('complaint-details', data.ticketId);
  } catch (err) {
    alert('Error submitting complaint: ' + err.message);
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span>Submit Issue</span>';
  }
}

// =============================================================================
// Screen 4: Complaint Details & Tracking (Student View)
// =============================================================================
async function loadComplaintDetails(ticketId) {
  state.activeTicketId = ticketId;
  const backLabel = document.getElementById('back-nav-label');
  if (state.user && (state.user.role === 'ROLE_STAFF' || state.user.role === 'ROLE_ADMIN')) {
    backLabel.innerText = 'Back to Staff Dashboard';
  } else {
    backLabel.innerText = 'Back to My Complaints';
  }

  try {
    const res = await authFetch(`${API_BASE}/api/complaints/${ticketId}`);
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Could not load complaint details');
    }

    const c = await res.json();

    document.getElementById('detail-ticket-id').innerText = c.ticketId;
    document.getElementById('detail-priority-badge').outerHTML = renderPriorityBadge(c.priorityLevel, c.priorityScore, 'detail-priority-badge');
    
    // AI/Fallback Source Badge
    const srcBadge = document.getElementById('detail-source-badge');
    if (srcBadge) {
      srcBadge.innerText = c.aiResultSource === 'FALLBACK_ENGINE' ? 'Fallback Engine' : 'AI Prioritized';
    }

    document.getElementById('detail-title').innerText = c.title;
    document.getElementById('detail-category').innerText = c.categoryDisplayName || c.category;
    document.getElementById('detail-location').innerText = c.location;
    document.getElementById('detail-submitted-date').innerText = formatDate(c.createdAt);
    document.getElementById('detail-description').innerText = c.description;

    // Attached photo
    const photoContainer = document.getElementById('detail-photo-container');
    const photoImg = document.getElementById('detail-photo-img');
    if (c.photoUrl) {
      photoContainer.style.display = 'block';
      photoImg.src = c.photoUrl;
    } else {
      photoContainer.style.display = 'none';
    }

    // Current Status Banner
    const statusBadge = document.getElementById('detail-current-status-badge');
    statusBadge.className = `badge badge-${c.status.toLowerCase().replace('_', '-')}`;
    statusBadge.innerText = c.status.replace('_', ' ');

    const assignedStaffEl = document.getElementById('detail-assigned-staff-name');
    assignedStaffEl.innerText = c.assignedStaffName ? `${c.assignedStaffName} (Maintenance)` : 'Campus Maintenance Desk';

    // Load QR Code thumbnail for complaint mobile tracking
    loadComplaintQrThumbnail(c.ticketId);

    // Build Vertical Stepper Timeline
    renderStepperTimeline(c.status, c.timeline);

    // Build Remarks Thread
    renderRemarks(c.remarks);

  } catch (err) {
    alert(err.message);
    navigateTo('my-complaints');
  }
}

async function loadComplaintQrThumbnail(ticketId) {
  try {
    const qrImg = document.getElementById('detail-qr-img');
    const res = await fetch(`${API_BASE}/api/qr/details/${encodeURIComponent(ticketId)}`);
    if (res.ok) {
      const data = await res.json();
      qrImg.src = data.qrBase64;
    }
  } catch (e) {
    console.warn('QR thumbnail error:', e);
  }
}

function renderStepperTimeline(currentStatus, timeline) {
  const stepper = document.getElementById('detail-stepper');
  const steps = ['SUBMITTED', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED'];

  const currentIndex = steps.indexOf(currentStatus);

  stepper.innerHTML = steps.map((step, idx) => {
    const isCompleted = idx < currentIndex;
    const isActive = idx === currentIndex;
    const historyItem = timeline ? timeline.find(h => h.newStatus === step) : null;

    let timeText = 'Pending staff action';
    let remarksText = '';
    if (historyItem) {
      timeText = formatDate(historyItem.timestamp);
      if (historyItem.remarks) {
        remarksText = `<div style="font-size: 0.75rem; color: var(--text-dim); margin-top: 2px;">${escapeHtml(historyItem.remarks)}</div>`;
      }
    } else if (isCompleted) {
      timeText = 'Completed';
    }

    return `
      <div class="step-item ${isActive ? 'active' : ''} ${isCompleted ? 'completed' : ''}">
        <div class="step-bullet">${isCompleted ? '✓' : ''}</div>
        <div style="font-weight: 700; color: #fff; font-size: 0.95rem;">${step.replace('_', ' ')}</div>
        <div style="font-size: 0.78rem; color: var(--text-muted);">${timeText}</div>
        ${remarksText}
      </div>
    `;
  }).join('');
}

function renderRemarks(remarks) {
  const container = document.getElementById('detail-remarks-container');
  if (!remarks || remarks.length === 0) {
    container.innerHTML = `<div style="text-align: center; color: var(--text-dim); font-size: 0.825rem; padding: 20px;">No remarks posted yet.</div>`;
    return;
  }

  container.innerHTML = remarks.map(r => `
    <div class="remark-bubble ${r.staffRemark ? 'staff' : 'student'}">
      <div style="font-weight: 700; color: ${r.staffRemark ? 'var(--cyan-primary)' : '#c084fc'}; font-size: 0.8rem; margin-bottom: 2px;">
        ${escapeHtml(r.authorName)} (${r.authorRole})
      </div>
      <div>${escapeHtml(r.content)}</div>
      <div class="remark-meta">${formatDate(r.timestamp)}</div>
    </div>
  `).join('');

  container.scrollTop = container.scrollHeight;
}

async function handleAddRemarkSubmit(e) {
  e.preventDefault();
  const input = document.getElementById('input-remark-text');
  const content = input.value.trim();
  if (!content || !state.activeTicketId) return;

  try {
    const res = await authFetch(`${API_BASE}/api/complaints/${state.activeTicketId}/remarks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content })
    });

    if (res.ok) {
      input.value = '';
      loadComplaintDetails(state.activeTicketId);
    } else {
      const err = await res.json();
      alert('Error posting remark: ' + (err.message || 'Forbidden'));
    }
  } catch (err) {
    alert('Failed to post remark: ' + err.message);
  }
}

function goBackFromDetails() {
  if (state.user && (state.user.role === 'ROLE_STAFF' || state.user.role === 'ROLE_ADMIN')) {
    navigateTo('staff-dashboard');
  } else {
    navigateTo('my-complaints');
  }
}

// =============================================================================
// Screen 5 & 6: Staff Dashboard & Complaint Management
// =============================================================================
async function loadStaffDashboard() {
  try {
    // 1. Fetch Staff Metrics
    const metricsRes = await authFetch(`${API_BASE}/api/analytics/staff`);
    if (metricsRes.ok) {
      const metrics = await metricsRes.json();
      document.getElementById('staff-stat-total').innerText = metrics.total || 0;
      document.getElementById('staff-stat-submitted').innerText = metrics.submitted || 0;
      document.getElementById('staff-stat-open').innerText = metrics.open || 0;
      document.getElementById('staff-stat-in-progress').innerText = metrics.inProgress || 0;
      document.getElementById('staff-stat-resolved').innerText = metrics.resolved || 0;
      document.getElementById('staff-stat-overdue').innerText = metrics.overdue || 0;

      // Render Donut & Bar Charts using Chart.js
      renderStaffCharts(metrics);
    }

    // 2. Fetch Complaints List
    await loadStaffComplaints();
  } catch (err) {
    console.error('Failed to load staff dashboard:', err);
  }
}

let debounceTimer = null;
function debounceStaffFilter() {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(loadStaffComplaints, 300);
}

async function loadStaffComplaints() {
  const query = document.getElementById('staff-search-input').value.trim();
  const status = document.getElementById('staff-filter-status').value;
  const category = document.getElementById('staff-filter-category').value;

  const url = `${API_BASE}/api/staff/complaints?query=${encodeURIComponent(query)}&status=${encodeURIComponent(status)}&category=${encodeURIComponent(category)}`;

  try {
    const res = await authFetch(url);
    if (res.ok) {
      const list = await res.json();
      const tbody = document.getElementById('staff-complaints-tbody');

      if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 28px; color: var(--text-dim);">No matching complaints found.</td></tr>`;
        return;
      }

      tbody.innerHTML = list.map(c => `
        <tr>
          <td><strong style="color:#fff;">${c.ticketId}</strong></td>
          <td>
            <div style="font-weight: 600; color: #fff;">${escapeHtml(c.title)}</div>
            <div style="font-size: 0.75rem; color: var(--text-dim);">${c.categoryDisplayName || c.category}</div>
          </td>
          <td>${escapeHtml(c.location)}</td>
          <td>
            <div style="display:flex; align-items:center; gap: 8px;">
              <span style="font-weight: 800; font-size: 0.95rem; color: #fff;">${c.priorityScore}</span>
              ${renderPriorityBadge(c.priorityLevel, c.priorityScore)}
            </div>
          </td>
          <td>${renderStatusBadge(c.status)}</td>
          <td style="font-size: 0.825rem; color: var(--text-muted);">${escapeHtml(c.studentName || 'Student')}</td>
          <td style="font-size: 0.825rem; color: var(--text-dim);">${formatDate(c.createdAt)}</td>
          <td>
            <button class="btn btn-primary btn-sm" onclick="navigateTo('staff-management', '${c.ticketId}')">
              Manage
            </button>
          </td>
        </tr>
      `).join('');
    }
  } catch (err) {
    console.error('Failed to load staff complaints:', err);
  }
}

function renderStaffCharts(metrics) {
  if (typeof Chart === 'undefined') return;

  // Chart 1: Donut Chart - Complaints by Status
  const ctxStatus = document.getElementById('chart-by-status');
  if (ctxStatus) {
    if (state.chartStatus) state.chartStatus.destroy();

    const statusMap = metrics.complaintsByStatus || {};
    const labels = ['Submitted', 'In Progress', 'Resolved', 'Overdue'];
    const data = [
      statusMap['Submitted'] || 0,
      statusMap['In Progress'] || 0,
      statusMap['Resolved'] || 0,
      statusMap['Overdue'] || 0
    ];

    state.chartStatus = new Chart(ctxStatus, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: ['#38bdf8', '#818cf8', '#34d399', '#fb7185'],
          borderWidth: 0,
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: { color: '#94a3b8', font: { size: 11 } }
          }
        },
        cutout: '68%'
      }
    });
  }

  // Chart 2: Bar Chart - Complaints by Category
  const ctxCategory = document.getElementById('chart-by-category');
  if (ctxCategory) {
    if (state.chartCategory) state.chartCategory.destroy();

    const categoryMap = metrics.complaintsByCategory || {};
    const catLabels = Object.keys(categoryMap);
    const catData = Object.values(categoryMap);

    state.chartCategory = new Chart(ctxCategory, {
      type: 'bar',
      data: {
        labels: catLabels,
        datasets: [{
          label: 'Complaints',
          data: catData,
          backgroundColor: 'rgba(0, 242, 254, 0.75)',
          borderRadius: 6,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: {
            ticks: { color: '#64748b', font: { size: 9 }, maxRotation: 45, minRotation: 45 },
            grid: { display: false }
          },
          y: {
            beginAtZero: true,
            ticks: { color: '#64748b', stepSize: 1 },
            grid: { color: 'rgba(255, 255, 255, 0.05)' }
          }
        }
      }
    });
  }
}

// Staff Management View (Screen 6)
async function loadStaffManagement(ticketId) {
  state.activeTicketId = ticketId;

  try {
    const res = await authFetch(`${API_BASE}/api/complaints/${ticketId}`);
    if (!res.ok) {
      throw new Error('Complaint not found');
    }

    const c = await res.json();

    document.getElementById('staff-manage-ticket-id').innerText = c.ticketId;
    document.getElementById('staff-manage-priority-badge').outerHTML = renderPriorityBadge(c.priorityLevel, c.priorityScore, 'staff-manage-priority-badge');
    document.getElementById('staff-manage-source-badge').innerText = c.aiResultSource === 'FALLBACK_ENGINE' ? 'Fallback Engine' : 'AI Analyzed';

    document.getElementById('staff-manage-title').innerText = c.title;
    document.getElementById('staff-manage-category').innerText = c.categoryDisplayName || c.category;
    document.getElementById('staff-manage-location').innerText = c.location;
    document.getElementById('staff-manage-student').innerText = `${c.studentName} (${c.studentRollOrStaffId || c.studentEmail})`;
    document.getElementById('staff-manage-date').innerText = formatDate(c.createdAt);
    document.getElementById('staff-manage-description').innerText = c.description;

    // Set Status dropdown to current status
    const statusSelect = document.getElementById('staff-manage-status-select');
    statusSelect.value = c.status;

    // Render Priority Box Breakdown (Screen 6 right card)
    document.getElementById('score-box-number').innerText = `${c.priorityScore} / 100`;
    document.getElementById('score-box-badge').outerHTML = renderPriorityBadge(c.priorityLevel, c.priorityScore, 'score-box-badge');

    const breakdownList = document.getElementById('score-box-breakdown');
    if (c.priorityExplanation) {
      const parts = c.priorityExplanation.split(' | ');
      breakdownList.innerHTML = parts.map(p => `<li class="score-breakdown-item">${escapeHtml(p.trim())}</li>`).join('');
    } else {
      breakdownList.innerHTML = `<li class="score-breakdown-item">Standard priority evaluation</li>`;
    }

    document.getElementById('score-box-department').innerText = c.aiDepartment || 'General Maintenance';
    document.getElementById('score-box-safety').innerText = c.aiSafetyConcern ? '⚠️ Yes (Urgent Hazard)' : 'No';

    // Render Status History Table
    const histTbody = document.getElementById('staff-manage-history-tbody');
    if (c.timeline && c.timeline.length > 0) {
      histTbody.innerHTML = c.timeline.map(h => `
        <tr>
          <td>${renderStatusBadge(h.newStatus)}</td>
          <td style="font-size: 0.825rem; color: var(--text-muted);">${formatDate(h.timestamp)}</td>
          <td style="font-size: 0.825rem; color: #fff;">${escapeHtml(h.actedByName || 'System')}</td>
          <td style="font-size: 0.825rem; color: var(--text-dim);">${escapeHtml(h.remarks || '-')}</td>
        </tr>
      `).join('');
    } else {
      histTbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color: var(--text-dim);">No history transitions recorded.</td></tr>`;
    }

  } catch (err) {
    alert('Error loading complaint management: ' + err.message);
    navigateTo('staff-dashboard');
  }
}

async function handleStatusUpdateSubmit(e) {
  e.preventDefault();
  if (!state.activeTicketId) return;

  const status = document.getElementById('staff-manage-status-select').value;
  const remarks = document.getElementById('staff-manage-remarks-input').value.trim();

  try {
    const res = await authFetch(`${API_BASE}/api/staff/complaints/${state.activeTicketId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status, remarks })
    });

    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.message || 'Failed to update status');
    }

    document.getElementById('staff-manage-remarks-input').value = '';
    showNotification(`Status updated to "${status.replace('_', ' ')}" successfully!`, 'success');

    // Reload management screen with updated data
    loadStaffManagement(state.activeTicketId);
  } catch (err) {
    alert('Error updating status: ' + err.message);
  }
}

// =============================================================================
// Screen 7: Campus Location QR Codes
// =============================================================================
async function loadQrCodesView() {
  const grid = document.getElementById('qr-locations-grid');
  grid.innerHTML = `<div style="color: var(--text-muted); padding: 20px;">Generating location QR codes...</div>`;

  try {
    const res = await fetch(`${API_BASE}/api/qr/all`);
    if (res.ok) {
      const list = await res.json();
      if (list.length > 0 && list[0].targetUrl) {
        try {
          const u = new URL(list[0].targetUrl);
          document.getElementById('qr-display-base-url').innerText = `${u.protocol}//${u.host}`;
        } catch(e) {}
      }

      grid.innerHTML = list.map(loc => `
        <div class="qr-card">
          <div style="font-weight: 700; font-size: 1.15rem; color: #fff;">${escapeHtml(loc.name)}</div>
          <div style="font-size: 0.78rem; color: var(--cyan-primary);">${escapeHtml(loc.building || 'Campus Zone')} &bull; ${escapeHtml(loc.floor || 'Ground Floor')}</div>
          
          <div class="qr-image-wrapper">
            <img src="${loc.qrBase64}" alt="QR code for ${escapeHtml(loc.name)}">
          </div>

          <div style="font-size: 0.72rem; color: var(--text-dim); word-break: break-all; max-width: 220px;">
            ${escapeHtml(loc.targetUrl)}
          </div>

          <div style="display: flex; gap: 8px; width: 100%; margin-top: 6px;">
            <a href="${loc.qrBase64}" download="QR-${loc.name}.png" class="btn btn-secondary btn-sm" style="flex:1;">
              Download
            </a>
            <button class="btn btn-outline-cyan btn-sm" style="flex:1;" onclick="testQrScan('${escapeHtml(loc.name)}')">
              Test Scan
            </button>
          </div>
        </div>
      `).join('');
    }
  } catch (err) {
    grid.innerHTML = `<div style="color: #f87171; padding: 20px;">Failed to load location QR codes: ${err.message}</div>`;
  }
}

function testQrScan(locationName) {
  showNotification(`Simulated Camera Scan for ${locationName}. Opening issue report form...`, 'info');
  navigateTo('report', locationName);
}

// =============================================================================
// Helper Functions & UI Utilities
// =============================================================================
function authFetch(url, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};
  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }
  return fetch(url, { ...options, headers });
}

function renderStatusBadge(status) {
  if (!status) return '';
  const key = status.toLowerCase().replace('_', '-');
  return `<span class="badge badge-${key}">${status.replace('_', ' ')}</span>`;
}

function renderPriorityBadge(level, score, idAttr = '') {
  const lvl = (level || 'Medium').toLowerCase();
  const idStr = idAttr ? `id="${idAttr}"` : '';
  return `<span ${idStr} class="badge badge-priority-${lvl}">${level || 'Medium'} Priority</span>`;
}

function formatDate(isoStr) {
  if (!isoStr) return '-';
  try {
    const d = new Date(isoStr);
    return d.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return isoStr;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function showNotification(msg, type = 'info') {
  const notif = document.getElementById('app-notification');
  notif.style.display = 'block';

  if (type === 'success') {
    notif.style.background = 'rgba(52, 211, 153, 0.15)';
    notif.style.border = '1px solid rgba(52, 211, 153, 0.4)';
    notif.style.color = '#34d399';
  } else if (type === 'error') {
    notif.style.background = 'rgba(239, 68, 68, 0.15)';
    notif.style.border = '1px solid rgba(239, 68, 68, 0.4)';
    notif.style.color = '#f87171';
  } else {
    notif.style.background = 'rgba(0, 242, 254, 0.15)';
    notif.style.border = '1px solid var(--border-glow)';
    notif.style.color = 'var(--cyan-primary)';
  }

  notif.innerText = msg;
  setTimeout(() => {
    notif.style.display = 'none';
  }, 5000);
}

function showError(elId, msg) {
  const el = document.getElementById(elId);
  if (el) {
    el.style.display = 'block';
    el.innerText = msg;
  }
}

function hideError(elId) {
  const el = document.getElementById(elId);
  if (el) el.style.display = 'none';
}

function togglePasswordVisibility(inputId) {
  const input = document.getElementById(inputId);
  if (input) {
    input.type = input.type === 'password' ? 'text' : 'password';
  }
}
