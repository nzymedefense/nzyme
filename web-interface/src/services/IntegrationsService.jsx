import RESTClient from '../util/RESTClient'

class IntegrationsService {

  getGeoIpSummary(successCallback) {
    RESTClient.get('/system/integrations/geoip', {}, successCallback)
  }

}

export default IntegrationsService
