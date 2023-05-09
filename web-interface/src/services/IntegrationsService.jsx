import RESTClient from '../util/RESTClient'

class IntegrationsService {

  getGeoIpSummary(successCallback) {
    RESTClient.get('/system/integrations/geoip', {}, successCallback)
  }

  activateGeoIpProvider(providerName, successCallback) {
    RESTClient.put('/system/integrations/geoip/providers/active', {provider_name: providerName}, successCallback)
  }

  getGeoIpIpInfoFreeConfiguration(setConfiguration) {
    RESTClient.get('/system/integrations/geoip/providers/ipinfofree/configuration', {}, function(response) {
      setConfiguration(response.data);
    })
  }

  updateGeoIpIpInfoFreeConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/integrations/geoip/providers/ipinfofree/configuration', { change: newConfig }, successCallback, errorCallback)
  }


}

export default IntegrationsService
