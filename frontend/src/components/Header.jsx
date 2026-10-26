import React from 'react';

export default function Header({
  activeTab,
  setActiveTab,
  currentUser,
  demoUsers,
  onSwitchUser,
  loadingUser,
}) {
  return (
    <header className="navbar">
      <div className="brand-section">
        <div className="brand-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span className="brand-title">KnowFlow AI</span>
            <span className="brand-badge">Permission-Aware RAG</span>
          </div>
          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
            Enterprise Knowledge & Security Agent
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <nav className="nav-tabs">
        <button
          className={`nav-tab-btn ${activeTab === 'chat' ? 'active' : ''}`}
          onClick={() => setActiveTab('chat')}
        >
          <span>💬</span> AI Search
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'security' ? 'active' : ''}`}
          onClick={() => setActiveTab('security')}
        >
          <span>🛡️</span> Security Proof Matrix
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'documents' ? 'active' : ''}`}
          onClick={() => setActiveTab('documents')}
        >
          <span>📁</span> Authorized Docs
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'audit' ? 'active' : ''}`}
          onClick={() => setActiveTab('audit')}
        >
          <span>📋</span> Audit Trail
        </button>
        <button
          className={`nav-tab-btn ${activeTab === 'architecture' ? 'active' : ''}`}
          onClick={() => setActiveTab('architecture')}
        >
          <span>📐</span> Architecture
        </button>
      </nav>

      {/* User Switcher */}
      <div className="role-switcher-wrapper">
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', marginRight: '0.25rem' }}>
          <span style={{ fontSize: '0.68rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: '700' }}>
            Simulate Persona
          </span>
          <select
            className="role-switcher-select"
            value={currentUser?.email || ''}
            onChange={(e) => onSwitchUser(e.target.value)}
            disabled={loadingUser}
          >
            {demoUsers.map((u) => (
              <option key={u.email} value={u.email}>
                {u.name} — {u.roleLabel} ({u.department})
              </option>
            ))}
          </select>
        </div>

        {currentUser && (
          <div className="active-user-badge">
            <div className="status-dot"></div>
            <div>
              <div style={{ fontWeight: '700', color: 'var(--text-primary)' }}>
                {currentUser.fullName}
              </div>
              <div style={{ fontSize: '0.66rem', color: 'var(--text-secondary)' }}>
                {currentUser.department} • {Array.from(currentUser.roles || []).join(', ')}
              </div>
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
