import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authService } from '../services/authService';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    if (!email.trim() || !password.trim()) return;
    if (!isLogin && !name.trim()) return;

    setLoading(true);
    setError(null);

    try {
      const result = isLogin
        ? await authService.login(email, password)
        : await authService.register(email, name, password);

      if (result.token) {
        navigate('/');
      } else {
        setError(result.message || t('login.authError'));
      }
    } catch (err) {
      const apiMessage = err.response?.data?.message;
      const normalizedMessage = typeof apiMessage === 'string' ? apiMessage.trim() : '';

      if (normalizedMessage) {
        setError(normalizedMessage);
      } else if (err.code === 'ECONNABORTED') {
        setError(t('login.serverTimeout'));
      } else if (!err.response) {
        setError(t('login.serverUnavailable'));
      } else if (err.response?.status === 401) {
        setError(t('login.invalidCredentials'));
      } else {
        setError(t('login.authError'));
      }
    } finally {
      setLoading(false);
    }
  };

  const switchMode = () => {
    setIsLogin(!isLogin);
    setError(null);
    setEmail('');
    setPassword('');
    setName('');
  };

  return (
    <div className="login-page">
      {/* ── Left editorial panel ── */}
      <div className="login-left">
        <div className="nav-brand" style={{ textDecoration: 'none', pointerEvents: 'none' }}>
          <span className="nav-brand-dot" />
          <span className="nav-brand-text">Task Scheduler AI</span>
        </div>

        <div style={{ display: 'flex', gap: '8px', marginTop: '18px', position: 'relative', zIndex: 1 }}>
          <button
            type="button"
            onClick={() => i18n.changeLanguage('pt')}
            className="btn btn-ghost"
          >
            {t('common.portuguese')}
          </button>
          <button
            type="button"
            onClick={() => i18n.changeLanguage('en')}
            className="btn btn-ghost"
          >
            {t('common.english')}
          </button>
        </div>

        <div style={{ position: 'relative', zIndex: 1 }}>
          <h1 className="login-headline">
            {t('login.headline_line1')}<br />
            <span dangerouslySetInnerHTML={{ __html: t('login.headline_line2') }} />
            <br />
            {t('login.headline_line3')}
          </h1>
        </div>

        <p className="login-subtext">
          {t('login.tagline')}
        </p>

        <div className="login-left-watermark" aria-hidden="true">AGENDA</div>
      </div>

      {/* ── Right form panel ── */}
      <div className="login-right">
        <div style={{ marginBottom: '40px' }}>
          <p className="section-counter" style={{ marginBottom: '14px' }}>
            {t('login.step')} {isLogin ? t('login.accessLabel') : t('login.registerLabel')}
          </p>
          <h2 className="login-form-title">
            {isLogin ? t('login.signIn') : t('login.createAccount')}
          </h2>
          <p className="login-form-sub">
            {isLogin
              ? t('login.signInSub')
              : t('login.registerSub')}
          </p>
        </div>

        {error && (
          <div className="error-bar" style={{ marginBottom: '24px' }}>{error}</div>
        )}

        <form
          onSubmit={handleSubmit}
          style={{ display: 'flex', flexDirection: 'column', gap: '22px' }}
        >
          <div className="field-group">
            <label className="field-label">{t('login.emailLabel')}</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder={t('login.emailPlaceholder')}
              className="field-input"
              required
            />
          </div>

          {!isLogin && (
            <div className="field-group">
              <label className="field-label">{t('login.nameLabel')}</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder={t('login.namePlaceholder')}
                className="field-input"
                required={!isLogin}
              />
            </div>
          )}

          <div className="field-group">
            <label className="field-label">{t('login.passwordLabel')}</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={t('login.passwordPlaceholder')}
              className="field-input"
              required
            />
          </div>

          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              marginTop: '10px',
            }}
          >
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center' }}
            >
              {loading ? (
                <>
                  {t('login.processing')}
                  <span className="loading-dots">
                    <span /><span /><span />
                  </span>
                </>
              ) : (
                isLogin ? t('login.signIn') : t('login.createAccount')
              )}
            </button>

            <button
              type="button"
              onClick={switchMode}
              className="btn btn-ghost"
              style={{ width: '100%', justifyContent: 'center' }}
            >
              {isLogin ? t('login.createNewAccount') : t('login.alreadyHaveAccount')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
