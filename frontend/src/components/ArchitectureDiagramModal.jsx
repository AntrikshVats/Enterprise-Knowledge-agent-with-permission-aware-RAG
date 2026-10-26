import React from 'react';

export default function ArchitectureDiagramModal() {
  const steps = [
    {
      num: '01',
      title: 'Authenticate Employee',
      tech: 'Spring Security & JJWT',
      desc: 'Validates the user identity from trusted server-side Bearer token and extracts user ID, roles, and department context.',
      security: 'Zero trust: Client claims are never trusted blindly; context is strictly bound server-side.'
    },
    {
      num: '02',
      title: 'Resolve Effective Permissions',
      tech: 'AccessControlService & JPA',
      desc: 'Evaluates the user ID, roles, department, and document ACLs to compute the exact set of authorized document IDs (e.g. [1, 4, 5]).',
      security: 'Admin bypass is strictly isolated; dynamic role revocations take effect instantaneously.'
    },
    {
      num: '03',
      title: 'Pre-Retrieval Vector Filter',
      tech: 'PostgreSQL + pgvector / VectorSearchService',
      desc: 'Executes similarity search with SQL filter: WHERE c.document_id IN (:authorizedDocIds). Chunks outside this set are physically never queried.',
      security: 'CRITICAL: Unauthorized passages never enter LLM memory. Anti-leakage guaranteed by database query.'
    },
    {
      num: '04',
      title: 'Grounded LLM Orchestration',
      tech: 'LlmService & Prompt Quarantining',
      desc: 'Retrieved chunks are quarantined inside <untrusted_document> XML delimiters. System prompt forbids following user instructions embedded in documents.',
      security: 'Prompt injection resistance: Treats all retrieved data strictly as passive evidence, never commands.'
    },
    {
      num: '05',
      title: 'Verified Citations & Audit Trail',
      tech: 'AuditService & Double Check',
      desc: 'Attaches verified citations with document titles, sections, and similarity scores. Re-checks authorization before opening excerpts and logs full audit trail.',
      security: 'Full traceability: Every query, retrieved chunk ID, and blocked breach attempt is logged for compliance.'
    }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Overview Banner */}
      <div style={{
        background: 'var(--bg-card)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-lg)',
        padding: '1.5rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <span style={{ fontSize: '1.4rem' }}>📐</span>
          <h2 style={{ fontSize: '1.3rem', fontWeight: '800' }}>
            KnowFlow AI — Permission-Aware RAG Architecture
          </h2>
          <span className="brand-badge" style={{ background: 'rgba(37, 99, 235, 0.15)', color: 'var(--accent-cyan)' }}>
            Enterprise Production Standard
          </span>
        </div>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
          In traditional RAG, systems retrieve unrestricted passages and attempt to filter answers post-generation,
          which allows unauthorized information to leak and enables prompt injection. KnowFlow AI enforces authorization
          <strong> before retrieval</strong> at the database vector index.
        </p>
      </div>

      {/* 5-Step Pipeline Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: '1rem'
      }}>
        {steps.map((st) => (
          <div
            key={st.num}
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: '1.25rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem',
              position: 'relative',
              overflow: 'hidden'
            }}
          >
            <div style={{
              position: 'absolute',
              top: '0.5rem',
              right: '0.75rem',
              fontSize: '2rem',
              fontWeight: '900',
              color: 'rgba(255, 255, 255, 0.04)',
              fontFamily: 'var(--font-mono)'
            }}>
              {st.num}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span className="sec-pill" style={{ color: 'var(--accent-cyan)', borderColor: 'var(--border-active)' }}>
                Step {st.num}
              </span>
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {st.tech}
              </span>
            </div>

            <h3 style={{ fontSize: '1.02rem', fontWeight: '700' }}>
              {st.title}
            </h3>

            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
              {st.desc}
            </p>

            <div style={{
              marginTop: 'auto',
              paddingTop: '0.75rem',
              borderTop: '1px solid var(--border-subtle)',
              fontSize: '0.72rem',
              color: 'var(--accent-emerald)',
              lineHeight: '1.45'
            }}>
              <strong>Security Rule: </strong>
              {st.security}
            </div>
          </div>
        ))}
      </div>

      {/* Database Schema & Enterprise Governance */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '1.25rem'
      }}>
        <div style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: '1.25rem'
        }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: '700', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span>🗄️</span> Database Schema & Access Control (JPA / SQL)
          </h4>
          <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <div><code>users</code>: id, email, password_hash, department, status</div>
            <div><code>roles</code> & <code>user_roles</code>: role_id, user_id (ROLE_ENGINEERING, ROLE_HR, etc.)</div>
            <div><code>documents</code>: id, title, department, classification, version, owner_id</div>
            <div><code>document_acl</code>: document_id, principal_type (ROLE/USER/DEPT), principal_id, permission</div>
            <div><code>document_chunks</code>: document_id, chunk_index, section_title, content, embedding_data</div>
            <div><code>audit_logs</code>: user_id, action, resource_id, details, status, timestamp</div>
          </div>
        </div>

        <div style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: '1.25rem'
        }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: '700', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span>💼</span> Enterprise Data Governance Standards
          </h4>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
            Enterprises process sensitive operational business data across heterogeneous systems (ERP, CRM, operational databases).
            Data governance, role-based visibility, query-level access control, and audit compliance are core requirements.
            KnowFlow AI adheres to these enterprise constraints: LLM generation is strictly secondary to deterministic backend authorization.
          </p>
        </div>
      </div>
    </div>
  );
}
