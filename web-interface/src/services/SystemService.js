import RESTClient from '../util/RESTClient'

class SystemService {

  getVersionInfo () {
    const self = this

    RESTClient.get('/system/version', {}, function (response) {
      self.setState({ versionInfo: response.data })
    })
  }
}

export default SystemService
