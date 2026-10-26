import React, { useState } from 'react';
import { api } from '../services/api';

export default function SecurityProofMatrix() {
  const [scenarios, setScenarios] = useState([]);
  const [running, setRunning] = useState(false);
  const [lastRunTimestamp, setLastRunTimestamp] = useState(null);

  const handleRunSuite = async () => {
    try {
      setRunning(true);
      const results = await api.runSecurityScenarios();
      setScenarios(results);
      setLastRunTimestamp(new Date().toLocaleTimeString());
    } catch (err) {
      alert('Failed to execute security evaluation suite: ' + err.message);
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="security-suite-container">
      {/* Header Banner */}
      <div className="suite-header-card">
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.3rem' }}>
            <span style={{ fontSize: '1.4rem' }}>🛡️</span>
            <h2 style={{ fontSize: '1.25rem', fontWeight: '800' }}>
              Enterprise Security & Isolation Verification Suite
            </h2>
            <span className="brand-badge" style={{ background: 'rgba(16, 185, 129, 0.15)', color: 'var(--accent-emerald)', borderColor: 'rgba(16, 185, 129, 0.3)' }}>
              Security Verification Suite
            </span>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', maxWidth: '750px', lineHeight: '1.5' }}>
            Prove that KnowFlow AI is an enterprise-grade backend knowledge service. This suite executes 5 live penetration
            and isolation scenarios directly against the Spring Boot security filter and vector retrieval engine.
          </p>
        </div>

        <button className="run-suite-btn" onClick={handleRunSuite} disabled={running}>
          <span>{running ? '⏳ Executing...' : '▶ Run 5 Security Scenarios'}</span>
        </button>
      </div>

      {lastRunTimestamp && (
        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <span>✓ Last executed at {lastRunTimestamp}</span>
          <span>•</span>
          <span>All 5 scenarios evaluated against live database & security context</span>
        </div>
      )}

      {/* Grid of Scenarios */}
      {scenarios.length === 0 ? (
        <div style={{
          background: 'var(--bg-card)',
          border: '1px dashed var(--border-active)',
          borderRadius: 'var(--radius-lg)',
          padding: '3rem 1.5rem',
          textAlign: 'center',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '1rem'
        }}>
          <div style={{ fontSize: '2.5rem' }}>🧪</div>
          <h3 style={{ fontSize: '1.15rem', fontWeight: '700' }}>No Security Test Run Yet</h3>
          <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', maxWidth: '500px' }}>
            Click the button above to run the automated test suite simulating unauthorized retrieval attempts,
            IDOR attacks, dynamic ACL revocations, and prompt injection payloads.
          </p>
          <button className="run-suite-btn" onClick={handleRunSuite} disabled={running}>
            <span>Run Security Suite Now</span>
          </button>
        </div>
      ) : (
        <div className="scenarios-grid">
          {scenarios.map((sc) => (
            <div key={sc.scenarioId} className="scenario-card">
              <div className="scenario-card-header">
                <div>
                  <div style={{ fontSize: '0.68rem', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                    {sc.scenarioId}
                  </div>
                  <h4 style={{ fontSize: '0.98rem', fontWeight: '700', color: 'var(--text-primary)' }}>
                    {sc.title}
                  </h4>
                </div>
                {sc.passed ? (
                  <span className="pass-badge">
                    <span>✓</span> PASS
                  </span>
                ) : (
                  <span className="fail-badge">
                    <span>✕</span> FAIL
                  </span>
                )}
              </div>

              {/* Actor & Action */}
              <div style={{
                background: 'var(--bg-tertiary)',
                borderRadius: 'var(--radius-sm)',
                padding: '0.65rem 0.85rem',
                fontSize: '0.76rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.25rem'
              }}>
                <div>
                  <strong style={{ color: 'var(--text-secondary)' }}>Tested Persona: </strong>
                  <span style={{ color: 'var(--text-primary)', fontWeight: '600' }}>
                    {sc.actorName} ({sc.actorRole})
                  </span>
                </div>
                <div>
                  <strong style={{ color: 'var(--text-secondary)' }}>Input / Action: </strong>
                  <span style={{ color: 'var(--accent-cyan)', fontFamily: 'var(--font-mono)' }}>
                    "{sc.queryOrAction}"
                  </span>
                </div>
              </div>

              {/* Expected vs Actual */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', fontSize: '0.78rem' }}>
                <div>
                  <span style={{ fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase', fontSize: '0.68rem' }}>
                    Expected Behavior:
                  </span>
                  <div style={{ color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
                    {sc.expectedBehavior}
                  </div>
                </div>

                <div>
                  <span style={{ fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase', fontSize: '0.68rem' }}>
                    Actual System Response:
                  </span>
                  <div style={{ color: sc.passed ? 'var(--accent-emerald)' : 'var(--accent-rose)', fontWeight: '600', marginTop: '0.15rem' }}>
                    {sc.actualBehavior}
                  </div>
                </div>
              </div>

              {/* Architectural Highlight */}
              <div style={{
                marginTop: 'auto',
                paddingTop: '0.65rem',
                borderTop: '1px solid var(--border-subtle)',
                fontSize: '0.72rem',
                color: 'var(--text-muted)',
                lineHeight: '1.45'
              }}>
                <strong style={{ color: 'var(--accent-cyan)' }}>Architectural Guarantee: </strong>
                {sc.explanation}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
