import React from 'react';

export default function CitationDrawer({ citation, onClose, onOpenFullDocument }) {
  if (!citation) return null;

  return (
    <div className="citation-drawer-overlay" onClick={onClose}>
      <div className="citation-drawer" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
              <span className={`classification-badge ${citation.classification}`}>
                {citation.classification}
              </span>
              <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                {citation.department} Department
              </span>
            </div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: '700', color: 'var(--text-primary)' }}>
              {citation.documentTitle}
            </h3>
          </div>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>

        <div className="drawer-body">
          {/* Verification Banner */}
          <div style={{
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            borderRadius: 'var(--radius-md)',
            padding: '0.85rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.65rem'
          }}>
            <span style={{ fontSize: '1.2rem' }}>🛡️</span>
            <div>
              <div style={{ fontSize: '0.82rem', fontWeight: '700', color: 'var(--accent-emerald)' }}>
                Pre-Retrieval Permission Verified
              </div>
              <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>
                This document ID ({citation.documentId}) was evaluated against your server-side security context before vector retrieval.
              </div>
            </div>
          </div>

          {/* Section & Relevance */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <div className="info-stat-card">
              <span className="info-stat-label">Section</span>
              <span style={{ fontSize: '0.9rem', fontWeight: '700', color: 'var(--text-primary)' }}>
                {citation.sectionTitle || 'General'}
              </span>
            </div>
            <div className="info-stat-card">
              <span className="info-stat-label">Vector Similarity</span>
              <span style={{ fontSize: '0.9rem', fontWeight: '700', color: 'var(--accent-cyan)' }}>
                {Math.round(citation.similarityScore * 100)}% Match
              </span>
            </div>
          </div>

          {/* Passage Excerpt */}
          <div>
            <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>
              Verified Passage Excerpt
            </div>
            <div style={{
              background: 'var(--bg-tertiary)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: '1rem',
              fontSize: '0.85rem',
              lineHeight: '1.6',
              color: 'var(--text-primary)',
              whiteSpace: 'pre-wrap'
            }}>
              {citation.excerpt}
            </div>
          </div>

          {/* Full Doc Action */}
          <button
            style={{
              marginTop: 'auto',
              background: 'var(--bg-tertiary)',
              border: '1px solid var(--border-active)',
              color: 'var(--accent-cyan)',
              padding: '0.75rem',
              borderRadius: 'var(--radius-md)',
              fontWeight: '700',
              fontSize: '0.84rem',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '0.5rem'
            }}
            onClick={() => onOpenFullDocument(citation.documentId)}
          >
            <span>📄</span> Open Full Document & Permissions Matrix
          </button>
        </div>
      </div>
    </div>
  );
}
