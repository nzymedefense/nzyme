import RESTClient from '../util/RESTClient'

class IntegrationsService {

  getGeoIpConfiguration(setHealthIndicators) {
    RESTClient.get('/system/health/indicators', {}, function (response) {
      setHealthIndicators(response.data.indicators)
    })
  }

}

export default SystemService
