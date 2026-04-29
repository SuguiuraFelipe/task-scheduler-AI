import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { conversationService } from '../services/taskService';

const parseAssistantResponse = (response) => {
  if (!response || typeof response !== 'string') {
    return null;
  }

  try {
    const parsed = JSON.parse(response);
    return parsed && typeof parsed === 'object' ? parsed : null;
  } catch (_) {
    return null;
  }
};

export default function ConversationHistory({ userId }) {
  const [conversations, setConversations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { t, i18n } = useTranslation();
  const priorityLabel = {
    URGENT: t('task.urgent'),
    HIGH: t('task.high'),
    MEDIUM: t('task.medium'),
    LOW: t('task.low'),
  };
  const priorityBadge = {
    URGENT: 'badge badge--urgent',
    HIGH: 'badge badge--high',
    MEDIUM: 'badge badge--medium',
    LOW: 'badge badge--low',
  };

  useEffect(() => {
    fetchConversations();
  }, [userId]);

  const fetchConversations = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const response = await conversationService.getConversations(userId);
      setConversations(response.data);
      setError(null);
    } catch (err) {
      setError(t('conversations.loadError'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          padding: '28px 0',
        }}
      >
        <span className="loading-dots">
          <span /><span /><span />
        </span>
        <span className="mono-label">{t('conversations.loading')}</span>
      </div>
    );
  }

  return (
    <div>
      {error && (
        <div className="error-bar" style={{ marginBottom: '16px' }}>
          {error}
        </div>
      )}

      {!error && conversations.length === 0 && (
        <div className="empty-state">
          <div className="empty-glyph">◌</div>
          <p className="empty-text">
            {t('conversations.empty')}
          </p>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {conversations.map((conv, i) => {
          const aiSuggestion = parseAssistantResponse(conv.assistantResponse);
          const dueDate = aiSuggestion?.dueDate
            ? new Date(aiSuggestion.dueDate).toLocaleDateString(i18n.resolvedLanguage || 'pt', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })
            : null;

          return (
            <div
              key={conv.id}
              className="conv-entry"
              style={{ animationDelay: `${i * 60}ms` }}
            >
              <div className="conv-timestamp">
                {new Date(conv.createdAt).toLocaleDateString(i18n.resolvedLanguage || 'pt', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </div>

              <div className="conv-block">
                <p className="conv-role conv-role--user">{t('conversations.you')}</p>
                <p className="conv-text">{conv.userMessage}</p>
              </div>

              <div className="conv-block">
                <p className="conv-role conv-role--ai">{t('conversations.ai')}</p>

                {aiSuggestion ? (
                  <div className="conv-ai-card">
                    <p className="conv-ai-title">
                      {aiSuggestion.title || t('ai.titleField')}
                    </p>

                    {aiSuggestion.description && (
                      <p className="conv-ai-description">{aiSuggestion.description}</p>
                    )}

                    <div className="conv-ai-meta">
                      {dueDate && (
                        <div className="conv-ai-meta-item">
                          <span className="conv-ai-meta-key">{t('ai.dueDateField')}</span>
                          <span className="conv-ai-meta-value">{dueDate}</span>
                        </div>
                      )}

                      {aiSuggestion.priority && (
                        <div className="conv-ai-meta-item">
                          <span className="conv-ai-meta-key">{t('ai.priorityField')}</span>
                          <span className={priorityBadge[aiSuggestion.priority] || 'badge badge--pending'}>
                            {priorityLabel[aiSuggestion.priority] || aiSuggestion.priority}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                ) : (
                  <p className="conv-text">{conv.assistantResponse}</p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
