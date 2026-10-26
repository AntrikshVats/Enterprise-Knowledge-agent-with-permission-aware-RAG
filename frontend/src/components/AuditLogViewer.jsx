import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function AuditLogViewer() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('ALL');

  const fetchLogs = async () => {
    try {
      setLoading(true);
      const data = await api.getAuditLogs();
      setLogs(data);
    } catch (err) {
      console.error('Failed to fetch audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const filteredLogs = logs.filter((log) => {
    if (filter === 'SECURITY') {
      return log.action.includes('DENIED') || log.action.includes('IDOR') || log.action.includes('INJECTION');
    }
    if (filter === 'QUERY') {
      return log.action === 'QUERY';
    }
    if (filter === 'ACL') {
      return log.action.includes('ACL');
    }
    return true;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header */}
      <div style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-lg)',
        padding: '1.25rem 1.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.25rem' }}>
            <span style={{ fontSize: '1.3rem' }}>📋</span>
            <h2 style={{ fontSize: '1.2rem', fontWeight: '800' }}>Enterprise Compliance & Audit Trail</h2>
            <span className="brand-badge" style={{ background: 'rgba(56, 189, 248, 0.15)', color: 'var(--accent-cyan)' }}>
              SOC 2 / ISO 27001 Log
            </span>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            Immutable ledger tracking all queries, retrieved chunk IDs, blocked IDOR attempts, dynamic ACL changes, and prompt injection triggers.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button
            className="nav-tab-btn"
            style={{ background: 'var(--bg-tertiary)' }}
            onClick={fetchLogs}
            disabled={loading}
          >
            <span>🔄</span> Refresh
          </button>
        </div>
      </div>

      {/* Filter Chips */}
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        {[
          { id: 'ALL', label: 'All Records' },
          { id: 'SECURITY', label: '🛡️ Security Alerts & IDOR Blocked' },
          { id: 'QUERY', label: '💬 Knowledge Queries' },
          { id: 'ACL', label: '🔑 ACL Modifications' },
        ].map((f) => (
          <button
            key={f.id}
            className={`suggestion-chip ${filter === f.id ? 'active' : ''}`}
            style={filter === f.id ? { background: 'rgba(56, 189, 248, 0.2)', borderColor: 'var(--accent-cyan)', color: 'var(--text-primary)' } : {}}
            onClick={() => setFilter(f.id)}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="audit-table-card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Loading audit records...
          </div>
        ) : filteredLogs.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            No audit records match the current filter.
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="audit-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Actor</th>
                  <th>Action</th>
                  <th>Resource</th>
                  <th>Status</th>
                  <th>Audit Details</th>
                </tr>
              </thead>
              <tbody>
                {filteredLogs.map((item) => (
                  <tr key={item.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.72rem', whiteSpace: 'nowrap' }}>
                      {new Date(item.createdAt).toLocaleTimeString()}
                    </td>
                    <td style={{ fontWeight: '600' }}>
                      {item.userEmail || 'system'}
                    </td>
                    <td>
                      <span className="sec-pill" style={{
                        borderColor: item.action.includes('IDOR') || item.action.includes('DENIED') ? 'var(--accent-rose)' : 'var(--border-subtle)',
                        color: item.action.includes('IDOR') || item.action.includes('DENIED') ? 'var(--accent-rose)' : 'var(--text-primary)'
                      }}>
                        {item.action}
                      </span>
                    </td>
                    <td style={{ fontSize: '0.75rem', fontFamily: 'var(--font-mono)' }}>
                      {item.resourceType} #{item.resourceId}
                    </td>
                    <td>
                      <span className={`status-badge ${item.status}`}>
                        {item.status}
                      </span>
                    </td>
                    <td style={{ maxWidth: '450px', fontSize: '0.75rem', lineHeight: '1.4' }}>
                      {item.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
