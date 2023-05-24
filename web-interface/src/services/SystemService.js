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

  updateHealthIndicatorsConfiguration(configuration, successCallback) {
    RESTClient.put('/system/health/indicators/configuration', configuration, successCallback)
  }

  findAllEvents(setEvents, limit, offset, eventTypes) {
    RESTClient.get('/system/events',
        {limit: limit, offset: offset, event_types: eventTypes.join(",")}, function (response) {
          setEvents(response.data);
        })
  }

}

export default SystemService
