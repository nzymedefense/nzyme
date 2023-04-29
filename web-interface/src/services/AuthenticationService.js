import RESTClient from '../util/RESTClient'

class AuthenticationService {
  createSession (username, password, successCallback, errorCallback) {

    RESTClient.post('/system/authentication/session', { username: username, password: password }, function (response) {
      successCallback(response.data.token);
    }, function (error) {
      if (error.response.status === 403) {
        errorCallback("Wrong credentials. Please try again.");
      } else {
        errorCallback("Login failed. Please try again.");
      }
    })
  }

  deleteSession(callback) {
    RESTClient.delete('/system/authentication/session', function () {
      callback();
    }, function () {
      // We also call the callback in case of an error because we delete the local session ID and the session will expire.
      callback();
    })
  }

  fetchSessionInfo(successCallback) {
    RESTClient.get('/system/authentication/session', {}, function(response) {
      successCallback(response.data);
    });
  }

  initializeMFASetup(setUserSecret, setUserEmail, setRecoveryCodes) {
    RESTClient.get('/system/authentication/mfa/setup/initialize', {}, function(response) {
      setUserSecret(response.data.user_secret);
      setUserEmail(response.data.user_email);
      setRecoveryCodes(response.data.recovery_codes);
    });
  }

  finishMFASetup() {
    RESTClient.post('/system/authentication/mfa/setup/complete', {}, function(){});
  }
}

export default AuthenticationService
