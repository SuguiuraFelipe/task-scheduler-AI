import api from './api';

export const taskService = {
  getAllTasks: (userId) => {
    return api.get('/tasks', { params: { userId } });
  },

  getTask: (taskId) => {
    return api.get(`/tasks/${taskId}`);
  },

  createTask: (taskData, userId) => {
    return api.post('/tasks', taskData, { params: { userId } });
  },

  createTaskFromPrompt: (prompt, userId) => {
    return api.post('/tasks/from-prompt', { prompt }, { params: { userId } });
  },

  updateTask: (taskId, taskData) => {
    return api.put(`/tasks/${taskId}`, taskData);
  },

  completeTask: (taskId) => {
    return api.patch(`/tasks/${taskId}/complete`);
  },

  deleteTask: (taskId) => {
    return api.delete(`/tasks/${taskId}`);
  },
};

export const userService = {
  register: (email, name) => {
    return api.post('/users/register', { email, name });
  },

  getUser: (userId) => {
    return api.get(`/users/${userId}`);
  },

  getUserByEmail: (email) => {
    return api.get(`/users/email/${email}`);
  },
};

export const conversationService = {
  suggestTask: (userMessage, userId) => {
    return api.post('/conversations/suggest-task', { userMessage }, { params: { userId } });
  },

  getConversations: (userId) => {
    return api.get('/conversations', { params: { userId } });
  },

  getConversationsByTask: (taskId) => {
    return api.get(`/conversations/task/${taskId}`);
  },
};
