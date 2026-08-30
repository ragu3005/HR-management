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
