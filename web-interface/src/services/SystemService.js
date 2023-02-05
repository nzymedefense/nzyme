import RESTClient from '../util/RESTClient'

class SystemService {

  getVersionInfo () {
    const self = this

    RESTClient.get('/system/version', {}, function (response) {
      self.setState({ versionInfo: response.data })
    })
  }

  getHealthIndicators(setHealthIndicators) {
    RESTClient.get('/system/health/indicators', {}, function (response) {
      setHealthIndicators(response.data.indicators)
    })
  }

}

export default SystemService
