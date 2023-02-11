import RESTClient from '../util/RESTClient'

class RetroService {
  getServiceSummary (setSummary) {
    RESTClient.get('/retro/service/summary', {}, function (response) {
      setSummary(response.data)
    })
  }

  getConfiguration (setConfiguration) {
    RESTClient.get('/retro/configuration', {}, function (response) {
      setConfiguration(response.data)
    })
  }

  updateConfiguration (newConfig, successCallback, errorCallback) {
    RESTClient.put('/retro/configuration', { change: newConfig }, successCallback, errorCallback)
  }
}

export default RetroService
