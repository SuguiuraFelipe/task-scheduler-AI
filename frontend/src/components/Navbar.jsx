import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authService } from '../services/authService';

export default function Navbar({ userId }) {
  const navigate = useNavigate();
  const location = useLocation();
  const currentUser = authService.getCurrentUser();
  const userName = currentUser.name || localStorage.getItem('userName');
  const { t, i18n } = useTranslation();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const handleLanguageChange = (language) => {
    i18n.changeLanguage(language);
  };

  return (
    <nav className="nav-bar">
      <div className="nav-inner">
        <Link to="/" className="nav-brand">
          <span className="nav-brand-dot" />
          <span className="nav-brand-text">Task Scheduler AI</span>
        </Link>

        <div className="nav-links">
          <div className="nav-language" aria-label={t('common.language')}>
            <button
              type="button"
              onClick={() => handleLanguageChange('pt')}
              className={`nav-link nav-link--button ${i18n.resolvedLanguage === 'pt' ? 'active' : ''}`}
            >
              {t('common.portuguese')}
            </button>
            <button
              type="button"
              onClick={() => handleLanguageChange('en')}
              className={`nav-link nav-link--button ${i18n.resolvedLanguage === 'en' ? 'active' : ''}`}
            >
              {t('common.english')}
            </button>
          </div>
          <Link
            to="/"
            className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
          >
            {t('nav.dashboard')}
          </Link>
          <Link
            to="/conversations"
            className={`nav-link ${location.pathname === '/conversations' ? 'active' : ''}`}
          >
            {t('nav.conversations')}
          </Link>
          {(userId || currentUser.userId) && (
            <>
              <span className="nav-user">{userName || `#${userId || currentUser.userId}`}</span>
              <button type="button" onClick={handleLogout} className="nav-logout">
                {t('nav.logout')}
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
