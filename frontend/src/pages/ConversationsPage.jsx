import React from 'react';
import { useTranslation } from 'react-i18next';
import { authService } from '../services/authService';
import ConversationHistory from '../components/ConversationHistory';

export default function ConversationsPage() {
  const userId = parseInt(authService.getCurrentUser().userId);
  const userName = authService.getCurrentUser().name;
  const { t } = useTranslation();

  return (
    <div style={{ maxWidth: '860px', margin: '0 auto', padding: '36px 32px' }}>
      <div
        style={{
          marginBottom: '40px',
          paddingBottom: '24px',
          borderBottom: '1px solid var(--border)',
        }}
      >
        <p className="section-counter" style={{ marginBottom: '10px' }}>
          {t('conversations.step')} {t('conversations.historyLabel')}
        </p>
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px' }}>
          <h1 className="section-heading" style={{ fontSize: '32px' }}>
            {t('conversations.heading')}
          </h1>
          {userName && (
            <span className="mono-label" style={{ color: 'var(--amber)' }}>
              {userName}
            </span>
          )}
        </div>
      </div>

      <ConversationHistory userId={userId} />
    </div>
  );
}
