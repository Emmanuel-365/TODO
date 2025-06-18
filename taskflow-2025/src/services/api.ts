import type { TodoList, Task } from '../types';

const API_BASE_URL = 'http://localhost:5050/api'; // Placeholder for Spring Boot API

// Simulate a delay to mimic network latency
const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Gestion du token JWT
function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('taskflow_token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

export const getLists = async (): Promise<TodoList[]> => {
  const response = await fetch(`${API_BASE_URL}/lists`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la récupération des listes');
  }
  return response.json();
};

export const createList = async (title: string): Promise<TodoList> => {
  const response = await fetch(`${API_BASE_URL}/lists`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: JSON.stringify({ title }),
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la création de la liste');
  }
  return response.json();
};

export const deleteList = async (listId: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/lists/${listId}`, {
    method: 'DELETE',
    headers: { ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la suppression de la liste');
  }
};

export const updateList = async (listToUpdate: TodoList): Promise<TodoList> => {
  const response = await fetch(`${API_BASE_URL}/lists/${listToUpdate.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: JSON.stringify(listToUpdate),
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la mise à jour de la liste');
  }
  return response.json();
};

export const createTask = async (listId: string, text: string): Promise<Task> => {
  const response = await fetch(`${API_BASE_URL}/lists/${listId}/tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: JSON.stringify({ text }),
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la création de la tâche');
  }
  return response.json();
};

export const updateTask = async (listId: string, task: Task): Promise<Task> => {
  const response = await fetch(`${API_BASE_URL}/lists/${listId}/tasks/${task.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: JSON.stringify(task),
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la mise à jour de la tâche');
  }
  return response.json();
};

export const deleteTask = async (listId: string, taskId: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/lists/${listId}/tasks/${taskId}`, {
    method: 'DELETE',
    headers: { ...getAuthHeaders() },
  });
  if (!response.ok) {
    throw new Error('Erreur lors de la suppression de la tâche');
  }
};

export interface AuthResponse {
  token: string;
}

export const register = async (email: string, name: string, password: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, name, password })
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'Erreur lors de l\'inscription');
  }
};

export const login = async (email: string, password: string): Promise<AuthResponse> => {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'Erreur lors de la connexion');
  }
  const data = await response.json();
  // Stocke le token JWT pour les prochaines requêtes
  localStorage.setItem('taskflow_token', data.token);
  return data;
};
