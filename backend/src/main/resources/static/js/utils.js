/**
 * HRMS Pro – Utilities & UI Helpers
 */

const Utils = {
  showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <span style="font-weight:600;">${type.toUpperCase()}</span>: ${message}
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  },

  formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const d = new Date(dateTimeStr);
    return d.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  },

  formatTime(timeStr) {
    if (!timeStr) return '-';
    const d = new Date(timeStr);
    if (isNaN(d.getTime())) return timeStr;
    return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  },

  getStatusBadge(status) {
    const s = (status || '').toUpperCase();
    switch (s) {
      case 'ACTIVE':
      case 'PRESENT':
      case 'APPROVED':
      case 'PUBLISHED':
        return `<span class="badge badge-success">${status}</span>`;
      case 'PENDING':
      case 'MANAGER_APPROVED':
      case 'HALF_DAY':
      case 'ON_NOTICE':
        return `<span class="badge badge-warning">${status.replace('_', ' ')}</span>`;
      case 'INACTIVE':
      case 'ABSENT':
      case 'REJECTED':
      case 'TERMINATED':
      case 'CANCELLED':
        return `<span class="badge badge-danger">${status}</span>`;
      case 'LATE':
      case 'ON_LEAVE':
        return `<span class="badge badge-info">${status.replace('_', ' ')}</span>`;
      default:
        return `<span class="badge badge-primary">${status || 'N/A'}</span>`;
    }
  },

  openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('show');
  },

  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('show');
  },

  toggleSidebar() {
    const sidebar = document.getElementById('app-sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (sidebar) {
      sidebar.classList.toggle('open');
      if (overlay) {
        overlay.classList.toggle('show');
      }
    }
  },

  renderSidebar(activePage = '') {
    const user = API.getCurrentUser() || {};
    const role = user.role || 'EMPLOYEE';
    const isSuperAdmin = role === 'SUPER_ADMIN';
    const isHR = isSuperAdmin || role === 'HR_ADMIN';
    const isManager = isHR || role === 'MANAGER';

    const sidebarHtml = `
      <div class="sidebar-overlay" id="sidebar-overlay" onclick="Utils.toggleSidebar()"></div>
      <div class="sidebar" id="app-sidebar">
        <div class="sidebar-header">
          <div class="brand-logo">H</div>
          <span class="brand-text">HRMS Pro</span>
        </div>

        <div class="sidebar-nav">
          <div class="nav-section-title">Core</div>
          <a href="dashboard.html" class="nav-item ${activePage === 'dashboard' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
            Dashboard
          </a>
          <a href="directory.html" class="nav-item ${activePage === 'directory' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            Directory
          </a>
          <a href="attendance.html" class="nav-item ${activePage === 'attendance' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
            Attendance
          </a>
          <a href="leave.html" class="nav-item ${activePage === 'leave' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            Leave
          </a>
          <a href="documents.html" class="nav-item ${activePage === 'documents' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
            Documents
          </a>
          <a href="announcements.html" class="nav-item ${activePage === 'announcements' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            Announcements
          </a>

          ${isHR ? `
            <div class="nav-section-title">HR Management</div>
            <a href="employees.html" class="nav-item ${activePage === 'employees' ? 'active' : ''}">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
              Employees
            </a>
            <a href="departments.html" class="nav-item ${activePage === 'departments' ? 'active' : ''}">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
              Departments
            </a>
            <a href="reports.html" class="nav-item ${activePage === 'reports' ? 'active' : ''}">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>
              Reports & Analytics
            </a>
          ` : ''}

          ${isSuperAdmin ? `
            <div class="nav-section-title">Administration</div>
            <a href="settings.html" class="nav-item ${activePage === 'settings' ? 'active' : ''}">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
              Settings
            </a>
            <a href="audit-logs.html" class="nav-item ${activePage === 'audit-logs' ? 'active' : ''}">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
              Audit Logs
            </a>
          ` : ''}
        </div>

        <div class="sidebar-footer">
          <div class="avatar">${(user.fullName || user.username || 'U').charAt(0).toUpperCase()}</div>
          <div class="user-meta" style="flex:1; overflow:hidden;">
            <div class="user-name" style="color:white; text-overflow:ellipsis; overflow:hidden; white-space:nowrap;">${user.fullName || user.username || 'User'}</div>
            <div class="user-role" style="color:#94a3b8;">${role.replace('_', ' ')}</div>
          </div>
          <button onclick="Auth.logout()" class="header-btn" title="Logout" style="color:#ef4444;">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
          </button>
        </div>
      </div>
    `;

    const el = document.getElementById('sidebar-mount');
    if (el) el.innerHTML = sidebarHtml;
  },

  renderHeader(title = 'HRMS Pro') {
    const user = API.getCurrentUser() || {};
    const headerHtml = `
      <header class="top-header">
        <div class="header-left" style="display:flex; align-items:center;">
          <button class="mobile-menu-btn" onclick="Utils.toggleSidebar()" title="Toggle Menu" style="display:none; background:none; border:none; color:var(--text-main); cursor:pointer; padding:6px; margin-right:12px; border-radius:var(--radius-sm);">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
          </button>
          <h1>${title}</h1>
        </div>
        <div class="header-right">
          <a href="notifications.html" class="header-btn" title="Notifications">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
            <span class="badge-dot" id="header-notif-dot" style="display:none;"></span>
          </a>
          <a href="employee-profile.html" class="user-profile-btn">
            <div class="avatar">${(user.fullName || user.username || 'U').charAt(0).toUpperCase()}</div>
            <div class="user-meta">
              <div class="user-name">${user.fullName || user.username || 'User'}</div>
              <div class="user-role">${(user.role || '').replace('_', ' ')}</div>
            </div>
          </a>
        </div>
      </header>
    `;
    const el = document.getElementById('header-mount');
    if (el) el.innerHTML = headerHtml;

    // Fetch unread notifications count
    API.get('/notifications/unread-count')
      .then(res => {
        if (res && res.data && res.data.unreadCount > 0) {
          const dot = document.getElementById('header-notif-dot');
          if (dot) dot.style.display = 'block';
        }
      })
      .catch(() => {});
  },

  initLayout(activePage, title) {
    Auth.requireAuth();
    this.renderSidebar(activePage);
    this.renderHeader(title);
  }
};

window.Utils = Utils;
