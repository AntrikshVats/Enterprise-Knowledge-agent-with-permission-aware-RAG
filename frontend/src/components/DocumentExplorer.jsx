import React, { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function DocumentExplorer({ currentUser }) {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedDoc, setSelectedDoc] = useState(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [showAclModal, setShowAclModal] = useState(false);

  // Upload Form State
  const [uploadTitle, setUploadTitle] = useState('');
  const [uploadDept, setUploadDept] = useState('ENGINEERING');
  const [uploadClassification, setUploadClassification] = useState('INTERNAL');
  const [uploadContent, setUploadContent] = useState('');

  // ACL Form State
  const [editingAcls, setEditingAcls] = useState([]);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const docs = await api.getDocuments();
      setDocuments(docs);
    } catch (err) {
      console.error('Failed to load documents:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, [currentUser]);

  const handleUpload = async (e) => {
    e.preventDefault();
    try {
      await api.uploadDocument({
        title: uploadTitle,
        description: 'Uploaded via KnowFlow Enterprise Portal',
        department: uploadDept,
        classification: uploadClassification,
        content: uploadContent,
      });
      setShowUploadModal(false);
      setUploadTitle('');
      setUploadContent('');
      await fetchDocuments();
      alert('Document ingested, chunked, and indexed with embeddings successfully!');
    } catch (err) {
      alert('Upload failed: ' + err.message);
    }
  };

  const handleOpenAclModal = (doc) => {
    setSelectedDoc(doc);
    setEditingAcls(doc.acls ? [...doc.acls] : []);
    setShowAclModal(true);
  };

  const handleSaveAcls = async () => {
    try {
      await api.updatePermissions(selectedDoc.id, editingAcls);
      setShowAclModal(false);
      await fetchDocuments();
      alert('Permissions updated successfully! Real-time dynamic ACL is now live.');
    } catch (err) {
      alert('Failed to update ACLs: ' + err.message);
    }
  };

  const handleAddAclRow = () => {
    setEditingAcls([
      ...editingAcls,
      { principalType: 'ROLE', principalId: 'ROLE_ENGINEERING', permission: 'READ' }
    ]);
  };

  const handleRemoveAclRow = (index) => {
    setEditingAcls(editingAcls.filter((_, i) => i !== index));
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      {/* Header Bar */}
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
            <span style={{ fontSize: '1.3rem' }}>📁</span>
            <h2 style={{ fontSize: '1.2rem', fontWeight: '800' }}>Authorized Enterprise Repository</h2>
            <span className="brand-badge">{documents.length} Visible Documents</span>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            Documents currently accessible by <strong>{currentUser?.fullName}</strong> ({currentUser?.department}) based on server-side Access Control Lists (ACLs).
          </p>
        </div>

        <button
          className="run-suite-btn"
          style={{ background: 'linear-gradient(135deg, var(--accent-blue), var(--accent-cyan))' }}
          onClick={() => setShowUploadModal(true)}
        >
          <span>＋ Upload & Index Document</span>
        </button>
      </div>

      {/* Documents Grid */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          Loading authorized repository...
        </div>
      ) : documents.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          No documents available in this user's authorized scope.
        </div>
      ) : (
        <div className="docs-grid">
          {documents.map((doc) => (
            <div key={doc.id} className="doc-card">
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span className={`classification-badge ${doc.classification}`}>
                  {doc.classification}
                </span>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                  ID: #{doc.id} • v{doc.version}
                </span>
              </div>

              <div>
                <h3 style={{ fontSize: '1.02rem', fontWeight: '700', marginBottom: '0.35rem' }}>
                  {doc.title}
                </h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                  {doc.description || doc.contentPreview}
                </p>
              </div>

              <div style={{
                background: 'var(--bg-tertiary)',
                borderRadius: 'var(--radius-sm)',
                padding: '0.65rem 0.85rem',
                fontSize: '0.74rem',
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '0.35rem',
                marginTop: 'auto'
              }}>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Dept: </span>
                  <strong>{doc.department}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Chunks: </span>
                  <strong>{doc.chunkCount} indexed</strong>
                </div>
              </div>

              {/* ACL Pills */}
              <div>
                <span style={{ fontSize: '0.68rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: '700' }}>
                  Active ACLs:
                </span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.3rem', marginTop: '0.25rem' }}>
                  {doc.acls && doc.acls.map((acl, i) => (
                    <span key={i} className="sec-pill" style={{ fontSize: '0.66rem' }}>
                      {acl.principalType}:{acl.principalId} ({acl.permission})
                    </span>
                  ))}
                </div>
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem', borderTop: '1px solid var(--border-subtle)', paddingTop: '0.65rem' }}>
                <button
                  style={{
                    flex: 1,
                    background: 'rgba(56, 189, 248, 0.1)',
                    border: '1px solid rgba(56, 189, 248, 0.25)',
                    color: 'var(--accent-cyan)',
                    padding: '0.45rem',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: '0.78rem',
                    fontWeight: '600',
                    cursor: 'pointer'
                  }}
                  onClick={() => setSelectedDoc(doc)}
                >
                  Inspect Content
                </button>
                <button
                  style={{
                    background: 'var(--bg-tertiary)',
                    border: '1px solid var(--border-subtle)',
                    color: 'var(--text-secondary)',
                    padding: '0.45rem 0.75rem',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: '0.78rem',
                    fontWeight: '600',
                    cursor: 'pointer'
                  }}
                  onClick={() => handleOpenAclModal(doc)}
                >
                  Edit ACLs
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Inspect Document Modal */}
      {selectedDoc && !showAclModal && (
        <div className="modal-overlay" onClick={() => setSelectedDoc(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <span className={`classification-badge ${selectedDoc.classification}`}>
                  {selectedDoc.classification}
                </span>
                <h3 style={{ fontSize: '1.15rem', fontWeight: '700', marginTop: '0.3rem' }}>
                  {selectedDoc.title}
                </h3>
              </div>
              <button className="close-btn" onClick={() => setSelectedDoc(null)}>✕</button>
            </div>
            <div className="modal-body">
              <div style={{
                background: 'var(--bg-tertiary)',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem',
                fontSize: '0.86rem',
                lineHeight: '1.65',
                whiteSpace: 'pre-wrap'
              }}>
                {selectedDoc.contentPreview || 'No content preview available.'}
              </div>
            </div>
            <div className="modal-footer">
              <button
                className="nav-tab-btn"
                style={{ background: 'var(--bg-tertiary)' }}
                onClick={() => setSelectedDoc(null)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit ACL Modal */}
      {showAclModal && selectedDoc && (
        <div className="modal-overlay" onClick={() => setShowAclModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: '700' }}>
                  Manage ACLs: {selectedDoc.title}
                </h3>
                <span style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                  Modifications apply immediately to pre-retrieval vector queries in real-time.
                </span>
              </div>
              <button className="close-btn" onClick={() => setShowAclModal(false)}>✕</button>
            </div>
            <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {editingAcls.map((acl, idx) => (
                <div key={idx} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                  <select
                    className="role-switcher-select"
                    value={acl.principalType}
                    onChange={(e) => {
                      const updated = [...editingAcls];
                      updated[idx].principalType = e.target.value;
                      setEditingAcls(updated);
                    }}
                  >
                    <option value="ROLE">ROLE</option>
                    <option value="DEPARTMENT">DEPARTMENT</option>
                    <option value="USER">USER</option>
                    <option value="PUBLIC">PUBLIC</option>
                  </select>

                  <input
                    type="text"
                    className="chat-input"
                    style={{ padding: '0.45rem 0.75rem', fontSize: '0.82rem' }}
                    value={acl.principalId}
                    placeholder="e.g. ROLE_ENGINEERING or *"
                    onChange={(e) => {
                      const updated = [...editingAcls];
                      updated[idx].principalId = e.target.value;
                      setEditingAcls(updated);
                    }}
                  />

                  <select
                    className="role-switcher-select"
                    value={acl.permission}
                    onChange={(e) => {
                      const updated = [...editingAcls];
                      updated[idx].permission = e.target.value;
                      setEditingAcls(updated);
                    }}
                  >
                    <option value="READ">READ</option>
                    <option value="WRITE">WRITE</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>

                  <button
                    style={{ background: 'transparent', border: 'none', color: 'var(--accent-rose)', cursor: 'pointer', fontSize: '1.1rem' }}
                    onClick={() => handleRemoveAclRow(idx)}
                  >
                    ✕
                  </button>
                </div>
              ))}

              <button
                className="nav-tab-btn"
                style={{ width: 'fit-content', background: 'rgba(255,255,255,0.06)', marginTop: '0.5rem' }}
                onClick={handleAddAclRow}
              >
                ＋ Add Principal Rule
              </button>
            </div>
            <div className="modal-footer">
              <button
                className="nav-tab-btn"
                style={{ background: 'var(--bg-tertiary)' }}
                onClick={() => setShowAclModal(false)}
              >
                Cancel
              </button>
              <button
                className="send-btn"
                style={{ padding: '0.5rem 1.25rem' }}
                onClick={handleSaveAcls}
              >
                Save & Apply Dynamic ACL
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Upload Document Modal */}
      {showUploadModal && (
        <div className="modal-overlay" onClick={() => setShowUploadModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <form onSubmit={handleUpload}>
              <div className="modal-header">
                <div>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: '700' }}>
                    Upload & Index Enterprise Document
                  </h3>
                  <span style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                    Auto-chunks by markdown sections and generates 384-dimensional dense semantic vectors.
                  </span>
                </div>
                <button type="button" className="close-btn" onClick={() => setShowUploadModal(false)}>✕</button>
              </div>

              <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ fontSize: '0.76rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: '700' }}>
                    Document Title
                  </label>
                  <input
                    type="text"
                    required
                    className="chat-input"
                    style={{ width: '100%', marginTop: '0.3rem' }}
                    placeholder="e.g. Kubernetes Cluster Disaster Recovery Runbook"
                    value={uploadTitle}
                    onChange={(e) => setUploadTitle(e.target.value)}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div>
                    <label style={{ fontSize: '0.76rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: '700' }}>
                      Department
                    </label>
                    <select
                      className="role-switcher-select"
                      style={{ width: '100%', marginTop: '0.3rem', padding: '0.7rem' }}
                      value={uploadDept}
                      onChange={(e) => setUploadDept(e.target.value)}
                    >
                      <option value="ENGINEERING">ENGINEERING</option>
                      <option value="HR">HR</option>
                      <option value="SECURITY">SECURITY</option>
                      <option value="GENERAL">GENERAL</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ fontSize: '0.76rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: '700' }}>
                      Security Classification
                    </label>
                    <select
                      className="role-switcher-select"
                      style={{ width: '100%', marginTop: '0.3rem', padding: '0.7rem' }}
                      value={uploadClassification}
                      onChange={(e) => setUploadClassification(e.target.value)}
                    >
                      <option value="INTERNAL">INTERNAL</option>
                      <option value="CONFIDENTIAL">CONFIDENTIAL</option>
                      <option value="RESTRICTED">RESTRICTED</option>
                      <option value="PUBLIC">PUBLIC</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label style={{ fontSize: '0.76rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: '700' }}>
                    Document Content (Markdown supported)
                  </label>
                  <textarea
                    required
                    className="chat-input"
                    rows={8}
                    style={{ width: '100%', marginTop: '0.3rem', resize: 'vertical' }}
                    placeholder="# Main Title&#10;&#10;## Section One&#10;Describe procedural specifications and operational instructions..."
                    value={uploadContent}
                    onChange={(e) => setUploadContent(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="nav-tab-btn"
                  style={{ background: 'var(--bg-tertiary)' }}
                  onClick={() => setShowUploadModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="send-btn"
                  style={{ padding: '0.5rem 1.25rem' }}
                >
                  Chunk & Index
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
