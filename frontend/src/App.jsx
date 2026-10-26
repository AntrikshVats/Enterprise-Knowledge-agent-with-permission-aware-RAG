import React, { useState, useEffect } from 'react';
import { api, setAuthToken } from './services/api';
import Header from './components/Header';
import ChatInterface from './components/ChatInterface';
import CitationDrawer from './components/CitationDrawer';
import SecurityProofMatrix from './components/SecurityProofMatrix';
import DocumentExplorer from './components/DocumentExplorer';
import AuditLogViewer from './components/AuditLogViewer';
import ArchitectureDiagramModal from './components/ArchitectureDiagramModal';

export default function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [currentUser, setCurrentUser] = useState(null);
  const [demoUsers, setDemoUsers] = useState([]);
  const [loadingUser, setLoadingUser] = useState(true);

  // Chat State
  const [messages, setMessages] = useState([]);
  const [chatLoading, setChatLoading] = useState(false);
  const [activeCitation, setActiveCitation] = useState(null);
  const [authorizedDocsCount, setAuthorizedDocsCount] = useState(0);

  // Initialize demo users and log in default user (Alice)
  useEffect(() => {
    const initApp = async () => {
      try {
        setLoadingUser(true);
        const users = await api.getDemoUsers();
        setDemoUsers(users);

        // Default sign-in: Alice Chen (Engineering)
        const defaultEmail = 'alice.engineer@knowflow.ai';
        const authData = await api.login(defaultEmail, 'password123');
        setCurrentUser(authData.user);

        // Load authorized docs count
        const docs = await api.getDocuments();
        setAuthorizedDocsCount(docs.length);
      } catch (err) {
        console.error('Initialization error:', err);
      } finally {
        setLoadingUser(false);
      }
    };

    initApp();
  }, []);

  // Switch persona handler
  const handleSwitchUser = async (email) => {
    try {
      setLoadingUser(true);
      const authData = await api.login(email, 'password123');
      setCurrentUser(authData.user);

      // Refresh documents scope for new user
      const docs = await api.getDocuments();
      setAuthorizedDocsCount(docs.length);

      // Reset active citation
      setActiveCitation(null);
    } catch (err) {
      alert('Failed to switch user: ' + err.message);
    } finally {
      setLoadingUser(false);
    }
  };

  // Send question
  const handleSendMessage = async (question) => {
    try {
      setChatLoading(true);
      const response = await api.askQuestion(question);
      setMessages((prev) => [...prev, response]);
    } catch (err) {
      alert('Error querying knowledge agent: ' + err.message);
    } finally {
      setChatLoading(false);
    }
  };

  return (
    <div className="app-layout">
      {/* Header */}
      <Header
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        currentUser={currentUser}
        demoUsers={demoUsers}
        onSwitchUser={handleSwitchUser}
        loadingUser={loadingUser}
      />

      {/* Security Governance Ribbon */}
      <div className="security-banner">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <span>🔒 <strong>Pre-Retrieval Authorization Query Active:</strong></span>
          <span>Zero unauthorized chunks enter LLM memory. Filtered directly in database retrieval.</span>
        </div>
        <div className="security-pills">
          <span className="sec-pill">SQL Predicate Isolation</span>
          <span className="sec-pill">Anti-IDOR Guard</span>
          <span className="sec-pill">Prompt Injection Sanitized</span>
        </div>
      </div>

      {/* Main Content Area */}
      <main className="main-container">
        {activeTab === 'chat' && (
          <ChatInterface
            currentUser={currentUser}
            messages={messages}
            loading={chatLoading}
            onSendMessage={handleSendMessage}
            onOpenCitation={(citation) => setActiveCitation(citation)}
            authorizedDocsCount={authorizedDocsCount}
          />
        )}

        {activeTab === 'security' && <SecurityProofMatrix />}

        {activeTab === 'documents' && <DocumentExplorer currentUser={currentUser} />}

        {activeTab === 'audit' && <AuditLogViewer />}

        {activeTab === 'architecture' && <ArchitectureDiagramModal />}
      </main>

      {/* Citation Detail Drawer */}
      <CitationDrawer
        citation={activeCitation}
        onClose={() => setActiveCitation(null)}
        onOpenFullDocument={() => {
          setActiveCitation(null);
          setActiveTab('documents');
        }}
      />
    </div>
  );
}
