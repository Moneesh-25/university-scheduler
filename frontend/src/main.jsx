import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("ErrorBoundary caught:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '40px 20px', fontFamily: 'sans-serif', maxWidth: 800, margin: '60px auto', background: '#ffffff', border: '1px solid #fca5a5', borderRadius: 12, boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
          <h2 style={{ color: '#dc2626', fontSize: '1.25rem', fontWeight: 700, marginBottom: 8 }}>Application Render Error</h2>
          <p style={{ color: '#4b5563', fontSize: '0.875rem', marginBottom: 16 }}>An error occurred while rendering the page component:</p>
          <pre style={{ background: '#fef2f2', padding: 16, borderRadius: 8, color: '#991b1b', overflow: 'auto', fontSize: '0.8rem', lineHeight: 1.5 }}>
            {this.state.error?.stack || this.state.error?.toString()}
          </pre>
          <button
            onClick={() => window.location.reload()}
            style={{ marginTop: 16, padding: '10px 20px', background: '#2563eb', color: '#ffffff', border: 'none', borderRadius: 6, fontWeight: 600, cursor: 'pointer' }}
          >
            Reload Interface
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>
);
