import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authService } from '../services/authService';
import TaskForm from '../components/TaskForm';
import TaskList from '../components/TaskList';
import AIChat from '../components/AIChat';

export default function Dashboard() {
  const currentUser = authService.getCurrentUser();
  const userId = parseInt(currentUser.userId);
  const { t } = useTranslation();
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleTaskCreated = useCallback(() => {
    setRefreshTrigger((prev) => prev + 1);
  }, []);

  return (
    <div className="dashboard-wrap">
      <div className="dashboard-header">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <span className="section-counter">{t('task.historyStep')}</span>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'baseline', gap: '12px' }}>
            <h1 className="section-heading" style={{ fontSize: '34px', margin: 0 }}>
              {t('task.historyHeading')}
            </h1>
            {currentUser.name && (
              <span className="mono-label" style={{ color: 'var(--amber)' }}>
                {currentUser.name}
              </span>
            )}
          </div>
          <p style={{ margin: 0, color: 'var(--text-2)', fontSize: '11px' }}>
            {t('task.historyDescription')}
          </p>
        </div>

        <Link to="/conversations" className="btn btn-ghost" style={{ justifyContent: 'center' }}>
          {t('task.openHistory')}
        </Link>
      </div>

      <div className="dashboard-grid">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '36px' }}>
          <TaskForm userId={userId} onTaskCreated={handleTaskCreated} />
          <TaskList userId={userId} onRefresh={refreshTrigger} />
        </div>

        <div style={{ position: 'sticky', top: '68px' }}>
          <AIChat userId={userId} onTaskCreated={handleTaskCreated} />
        </div>
      </div>
    </div>
  );
}
