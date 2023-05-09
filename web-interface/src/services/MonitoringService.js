import RESTClient from '../util/RESTClient'

class MonitoringService {
  getSummary (setSummary) {
    RESTClient.get('/system/monitoring/summary', {}, function (response) {
      setSummary(response.data)
    })
  }

  getPrometheusExporterConfiguration (setConfiguration) {
    RESTClient.get('/system/monitoring/prometheus/configuration', {}, function (response) {
      setConfiguration(response.data)
    })
  }

  updatePrometheusExporterConfiguration (newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/monitoring/prometheus/configuration', { change: newConfig }, successCallback, errorCallback)
  }
}

export default MonitoringService
