import RESTClient from '../util/RESTClient'
import Store from '../util/Store'

class AuthenticationService {
  createSession (username, password) {
    const self = this

    RESTClient.post('/authentication/session', { username: username, password: password }, function (response) {
      Store.set('api_token', response.data.token)
    }, function (response) {
      self.setState({ loggingIn: false })
    })
  }

  checkSession () {
    RESTClient.get('/authentication/session/information', {}, function (response) {
      if (response.data.seconds_left_valid <= 60) {
        Store.delete('api_token')
      }
    })
  }
}

export default AuthenticationService
