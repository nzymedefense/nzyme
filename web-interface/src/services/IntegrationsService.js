import RESTClient from '../util/RESTClient'

class IntegrationsService {

  getSmtpConfiguration(setConfiguration) {
    RESTClient.get('/system/integrations/smtp/configuration', {}, function(response) {
      setConfiguration(response.data);
    })
  }

  updateSmtpConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/integrations/smtp/configuration', { change: newConfig }, successCallback, errorCallback)
  }

}

export default IntegrationsService
