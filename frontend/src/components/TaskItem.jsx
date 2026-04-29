import React from 'react';
import { useTranslation } from 'react-i18next';

const PRIORITY_BAR = {
  URGENT: 'task-priority-bar task-priority-bar--urgent',
  HIGH:   'task-priority-bar task-priority-bar--high',
  MEDIUM: 'task-priority-bar task-priority-bar--medium',
  LOW:    'task-priority-bar task-priority-bar--low',
};

const PRIORITY_BADGE = {
  URGENT: 'badge badge--urgent',
  HIGH:   'badge badge--high',
  MEDIUM: 'badge badge--medium',
  LOW:    'badge badge--low',
};

const STATUS_BADGE = {
  COMPLETED:   'badge badge--completed',
  IN_PROGRESS: 'badge badge--in_progress',
  PENDING:     'badge badge--pending',
  CANCELLED:   'badge badge--cancelled',
};

export default function TaskItem({ task, onDelete, deleting = false }) {
  const { t, i18n } = useTranslation();
  const priorityLabel = {
    URGENT: t('task.urgent'),
    HIGH: t('task.high'),
    MEDIUM: t('task.medium'),
    LOW: t('task.low'),
  };
  const statusLabel = {
    COMPLETED: t('task.completed'),
    IN_PROGRESS: t('task.inProgress'),
    PENDING: t('task.pending'),
    CANCELLED: t('task.cancelled'),
  };
  const dueDate = new Date(task.dueDate).toLocaleDateString(i18n.resolvedLanguage || 'pt', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const isOverdue =
    new Date(task.dueDate) < new Date() && task.status !== 'COMPLETED';

  return (
    <div className={`task-card ${isOverdue ? 'task-card--overdue' : ''}`}>
      {/* Left priority signal bar */}
      <div className={PRIORITY_BAR[task.priority] || 'task-priority-bar'} />

      {/* Content */}
      <div className="task-content">
        <h3 className="task-title">{task.title}</h3>

        {task.description && (
          <p className="task-description">{task.description}</p>
        )}

        <div className="task-meta">
          <span className={PRIORITY_BADGE[task.priority] || 'badge badge--pending'}>
            {priorityLabel[task.priority] || task.priority}
          </span>
          <span className={STATUS_BADGE[task.status] || 'badge badge--pending'}>
            {statusLabel[task.status] || task.status}
          </span>
          {isOverdue && (
            <span className="badge badge--overdue">{t('task.overdue')}</span>
          )}
        </div>

        <p className="task-timestamp">
          {isOverdue ? '⚠ ' : ''}{t('task.dueOn')} {dueDate}
        </p>
      </div>

      {/* Delete action */}
      <div className="task-actions">
        <button
          type="button"
          onClick={(event) => {
            event.preventDefault();
            event.stopPropagation();
            onDelete(task.id);
          }}
          className="btn btn-danger"
          disabled={deleting}
          aria-label={`${t('task.remove')} ${task.title}`}
        >
          {deleting ? t('task.removing') : t('task.remove')}
        </button>
      </div>
    </div>
  );
}
