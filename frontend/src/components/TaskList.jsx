import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { taskService } from '../services/taskService';
import TaskItem from './TaskItem';

export default function TaskList({ userId, onRefresh }) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [deletingTaskId, setDeletingTaskId] = useState(null);
  const [completingTaskId, setCompletingTaskId] = useState(null);
  const { t } = useTranslation();

  useEffect(() => {
    fetchTasks();
  }, [userId, onRefresh]);

  const fetchTasks = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const response = await taskService.getAllTasks(userId);
      setTasks(response.data);
      setError(null);
    } catch (err) {
      setError(t('task.loadError'));
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (taskId) => {
    if (deletingTaskId === taskId) return;
    setDeletingTaskId(taskId);
    setError(null);

    try {
      await taskService.deleteTask(taskId);
      setTasks((prev) => prev.filter((t) => t.id !== taskId));
    } catch (err) {
      setError(t('task.deleteError'));
      console.error('Erro ao deletar tarefa:', err);
    } finally {
      setDeletingTaskId(null);
    }
  };

  const handleComplete = async (taskId) => {
    if (completingTaskId === taskId) return;
    setCompletingTaskId(taskId);
    setError(null);

    try {
      const response = await taskService.completeTask(taskId);
      setTasks((prev) =>
        prev.map((task) => (task.id === taskId ? response.data : task))
      );
    } catch (err) {
      setError(t('task.completeError'));
      console.error('Erro ao concluir tarefa:', err);
    } finally {
      setCompletingTaskId(null);
    }
  };

  return (
    <div>
      {/* Section header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'baseline',
          justifyContent: 'space-between',
          marginBottom: '18px',
          paddingBottom: '12px',
          borderBottom: '1px solid var(--border)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'baseline', gap: '12px' }}>
          <span className="section-counter">{t('task.step2')}</span>
          <span className="section-heading">{t('task.tasksHeading')}</span>
        </div>
        {tasks.length > 0 && (
          <span className="mono-label">
            {t('task.itemCount', { count: tasks.length })}
          </span>
        )}
      </div>

      {/* Loading */}
      {loading && (
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
          <span className="mono-label">{t('task.loading')}</span>
      </div>
      )}

      {/* Error */}
      {error && <div className="error-bar">{error}</div>}

      {/* Empty state */}
      {!loading && tasks.length === 0 && !error && (
        <div className="empty-state">
          <div className="empty-glyph">○</div>
          <p className="empty-text">{t('task.empty')}</p>
        </div>
      )}

      {/* Task list with stagger */}
      {!loading && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {tasks.map((task) => (
            <div key={task.id} className="task-list-item">
              <TaskItem
                task={task}
                onDelete={handleDelete}
                onComplete={handleComplete}
                deleting={deletingTaskId === task.id}
                completing={completingTaskId === task.id}
              />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
