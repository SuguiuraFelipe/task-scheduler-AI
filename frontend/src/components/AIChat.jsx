import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { conversationService, taskService } from '../services/taskService';

export default function AIChat({ userId, onTaskCreated }) {
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [suggestion, setSuggestion] = useState(null);
  const [error, setError] = useState(null);
  const { t, i18n } = useTranslation();
  const priorityLabel = {
    URGENT: t('task.urgent'),
    HIGH: t('task.high'),
    MEDIUM: t('task.medium'),
    LOW: t('task.low'),
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!message.trim() || !userId) return;

    setLoading(true);
    setError(null);

    try {
      const response = await conversationService.suggestTask(message, userId);
      setSuggestion(response.data);
      setMessage('');
    } catch (err) {
      setError(t('ai.suggestionError'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async () => {
    if (!suggestion || !userId) return;

    setLoading(true);
    try {
      await taskService.createTask(
        {
          title: suggestion.suggestedTitle,
          description: suggestion.suggestedDescription,
          dueDate: suggestion.suggestedDueDate,
          priority: suggestion.suggestedPriority,
        },
        userId
      );
      setSuggestion(null);
      onTaskCreated();
    } catch (err) {
      setError(t('ai.createError'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="terminal">
      {/* Terminal chrome */}
      <div className="terminal-header">
        <div className="terminal-title">
          <span className="terminal-dot" />
          {t('ai.terminalTitle')}
        </div>
        <span className="mono-label" style={{ fontSize: '9px', letterSpacing: '0.1em' }}>
          {t('ai.poweredBy')}
        </span>
      </div>

      <div className="terminal-body">
        <p className="terminal-hint">
          {t('ai.hint')}
        </p>

        {error && (
          <div className="error-bar">{error}</div>
        )}

        {/* Input form */}
        <form
          onSubmit={handleSubmit}
          style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}
        >
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder={t('ai.placeholder')}
            className="field-textarea"
            rows="4"
            disabled={loading}
          />

          <button
            type="submit"
            disabled={loading || !message.trim()}
            className="btn btn-primary"
            style={{ width: '100%', justifyContent: 'center' }}
          >
            {loading ? (
              <>
                {t('ai.analyzing')}
                <span className="loading-dots">
                  <span /><span /><span />
                </span>
              </>
            ) : (
              t('ai.getSuggestion')
            )}
          </button>
        </form>

        {/* AI suggestion result */}
        {suggestion && (
          <div className="terminal-suggestion">
            <p className="terminal-suggestion-label">{t('ai.suggestionLabel')}</p>

            <div className="terminal-field">
              <span className="terminal-field-key">{t('ai.titleField')}</span>
              <span className="terminal-field-val">{suggestion.suggestedTitle}</span>
            </div>

            {suggestion.suggestedDescription && (
              <div className="terminal-field">
                <span className="terminal-field-key">{t('ai.descriptionField')}</span>
                <span className="terminal-field-val terminal-field-val--small">
                  {suggestion.suggestedDescription}
                </span>
              </div>
            )}

            {suggestion.suggestedDueDate && (
              <div className="terminal-field">
                <span className="terminal-field-key">{t('ai.dueDateField')}</span>
                <span className="terminal-field-val terminal-field-val--small">
                  {new Date(suggestion.suggestedDueDate).toLocaleDateString(i18n.resolvedLanguage || 'pt', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </span>
              </div>
            )}

            <div className="terminal-field">
              <span className="terminal-field-key">{t('ai.priorityField')}</span>
              <span className="terminal-field-val">
                {priorityLabel[suggestion.suggestedPriority] || suggestion.suggestedPriority}
              </span>
            </div>

            <div style={{ display: 'flex', gap: '8px', marginTop: '16px' }}>
              <button
                onClick={handleCreateTask}
                disabled={loading}
                className="btn btn-success"
                style={{ flex: 1, justifyContent: 'center' }}
              >
                {loading ? t('ai.creatingTask') : t('ai.createTask')}
              </button>
              <button
                onClick={() => setSuggestion(null)}
                className="btn btn-ghost"
                style={{ justifyContent: 'center' }}
              >
                {t('ai.discard')}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
