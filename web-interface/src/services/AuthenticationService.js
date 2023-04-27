import RESTClient from '../util/RESTClient'
import Store from '../util/Store'

class AuthenticationService {
  createSession (username, password) {
    const self = this

    RESTClient.post('/system/authentication/session', { username: username, password: password }, function (response) {
      Store.set('api_token', response.data.token)
    }, function (response) {
      self.setState({ loggingIn: false })
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
}

export default AuthenticationService
