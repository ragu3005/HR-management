/**
 * HRMS Pro – Authentication Handling
 */

const Auth = {
  isLoggedIn() {
    return !!API.getToken();
  },

  requireAuth() {
    if (!this.isLoggedIn()) {
      window.location.href = 'index.html';
    }
  },

  async login(email, password) {
    const response = await API.post('/auth/login', { email, password });
    if (response && response.data) {
      API.setToken(response.data.token);
      API.setCurrentUser(response.data);
      return response.data;
    }
    throw new Error('Authentication failed');
  },

  async register(registerData) {
    const response = await API.post('/auth/register', registerData);
    if (response && response.data) {
      API.setToken(response.data.token);
      API.setCurrentUser(response.data);
      return response.data;
    }
    throw new Error(response.message || 'Registration failed');
  },

  async getRegistrationOptions() {
    const response = await API.get('/auth/registration-options');
    return response && response.data ? response.data : { departments: [], roles: [] };
  },

  logout() {
    API.post('/auth/logout', {}).catch(() => {});
    API.removeToken();
    window.location.href = 'index.html';
  },

  hasRole(role) {
    const user = API.getCurrentUser();
    return user && user.role === role;
  },

  hasAnyRole(...roles) {
    const user = API.getCurrentUser();
    return user && roles.includes(user.role);
  }
};

window.Auth = Auth;

