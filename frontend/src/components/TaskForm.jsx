import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { taskService } from '../services/taskService';

export default function TaskForm({ userId, onTaskCreated }) {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    dueDate: '',
    priority: 'MEDIUM',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { t } = useTranslation();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!userId) {
      setError(t('task.userRequired'));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await taskService.createTask(formData, userId);
      setFormData({ title: '', description: '', dueDate: '', priority: 'MEDIUM' });
      onTaskCreated();
    } catch (err) {
      setError(t('task.createError'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="panel">
      <div className="panel-header">
        <span className="section-counter">{t('task.step')}</span>
        <span className="section-heading">{t('task.newTask')}</span>
      </div>

      <div className="panel-body">
        {error && (
          <div className="error-bar" style={{ marginBottom: '20px' }}>
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          style={{ display: 'flex', flexDirection: 'column', gap: '22px' }}
        >
          <div className="field-group">
            <label className="field-label">{t('task.titleLabel')}</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              required
              className="field-input"
              placeholder={t('task.titlePlaceholder')}
            />
          </div>

          <div className="field-group">
            <label className="field-label">{t('task.descriptionLabel')}</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="field-textarea"
              placeholder={t('task.descriptionPlaceholder')}
              rows="3"
            />
          </div>

          <div
            style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}
          >
            <div className="field-group">
              <label className="field-label">{t('task.dueDateLabel')}</label>
              <input
                type="datetime-local"
                name="dueDate"
                value={formData.dueDate}
                onChange={handleChange}
                required
                className="field-input"
              />
            </div>

            <div className="field-group">
              <label className="field-label">{t('task.priorityLabel')}</label>
              <select
                name="priority"
                value={formData.priority}
                onChange={handleChange}
                className="field-input"
              >
                <option value="LOW">{t('task.low')}</option>
                <option value="MEDIUM">{t('task.medium')}</option>
                <option value="HIGH">{t('task.high')}</option>
                <option value="URGENT">{t('task.urgent')}</option>
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ marginTop: '4px' }}
          >
            {loading ? (
              <>
                {t('task.creating')}
                <span className="loading-dots">
                  <span /><span /><span />
                </span>
              </>
            ) : (
              t('task.createTask')
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
