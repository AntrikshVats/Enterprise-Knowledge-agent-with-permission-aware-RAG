const API_BASE = '/api';

export const getAuthToken = () => localStorage.getItem('knowflow_token');
export const setAuthToken = (token) => {
  if (token) localStorage.setItem('knowflow_token', token);
  else localStorage.removeItem('knowflow_token');
};

const request = async (endpoint, options = {}) => {
  const token = getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMsg = `Request failed (${response.status})`;
    try {
      const errorData = await response.json();
      errorMsg = errorData.message || errorData.error || errorMsg;
    } catch (e) {
      // ignore
    }
    const err = new Error(errorMsg);
    err.status = response.status;
    throw err;
  }

  // Check if no content
  if (response.status === 204) return null;
  return response.json();
};

export const api = {
  // Auth
  login: async (email, password = 'password123') => {
    const res = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    setAuthToken(res.token);
    return res;
  },

  getCurrentUser: () => request('/auth/me'),
  getDemoUsers: () => request('/auth/demo-users'),

  // Chat & RAG
  askQuestion: (question, sessionId = null) =>
    request('/chat/ask', {
      method: 'POST',
      body: JSON.stringify({ question, sessionId }),
    }),

  getSessions: () => request('/chat/sessions'),
  getSessionDetails: (id) => request(`/chat/sessions/${id}`),

  // Documents
  getDocuments: () => request('/documents'),
  getDocumentById: (id) => request(`/documents/${id}`),
  uploadDocument: (docData) =>
    request('/documents', {
      method: 'POST',
      body: JSON.stringify(docData),
    }),
  updatePermissions: (id, acls) =>
    request(`/documents/${id}/permissions`, {
      method: 'PUT',
      body: JSON.stringify({ acls }),
    }),

  // Security Proof Suite
  runSecurityScenarios: () => request('/demo/scenarios'),

  // Audit Logs
  getAuditLogs: () => request('/audit-logs'),
};
