/**
 * HRMS Pro – API Service Layer
 */

const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
  ? 'http://localhost:8080/api'
  : '/api';

class ApiService {
  static getToken() {
    return localStorage.getItem('hrms_token');
  }

  static setToken(token) {
    localStorage.setItem('hrms_token', token);
  }

  static removeToken() {
    localStorage.removeItem('hrms_token');
    localStorage.removeItem('hrms_user');
  }

  static getCurrentUser() {
    const userStr = localStorage.getItem('hrms_user');
    return userStr ? JSON.parse(userStr) : null;
  }

  static setCurrentUser(user) {
    localStorage.setItem('hrms_user', JSON.stringify(user));
  }

  static async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = this.getToken();

    const headers = {
      ...options.headers,
    };

    // If body is not FormData, add application/json Content-Type
    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (response.status === 401 && !endpoint.includes('/auth/login')) {
        this.removeToken();
        window.location.href = '/index.html';
        return null;
      }

      // Handle raw blobs/downloads
      if (options.responseType === 'blob') {
        if (!response.ok) throw new Error('File download failed');
        return await response.blob();
      }

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'An error occurred');
      }
      return data;
    } catch (error) {
      console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, error);
      throw error;
    }
  }

  static get(endpoint, params = {}) {
    const query = new URLSearchParams(params).toString();
    const url = query ? `${endpoint}?${query}` : endpoint;
    return this.request(url, { method: 'GET' });
  }

  static post(endpoint, body) {
    const isFormData = body instanceof FormData;
    return this.request(endpoint, {
      method: 'POST',
      body: isFormData ? body : JSON.stringify(body),
    });
  }

  static put(endpoint, body) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body),
    });
  }

  static patch(endpoint, body = {}) {
    return this.request(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(body),
    });
  }

  static delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

window.API = ApiService;
