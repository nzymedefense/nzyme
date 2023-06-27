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

  findAllEvents(setEvents, limit, offset, eventTypes, organizationId = undefined) {
    const params = {limit: limit, offset: offset, event_types: eventTypes.join(",")};

    if (organizationId) {
      params["organization_id"] = organizationId;
    }

    RESTClient.get('/system/events',
        params, function (response) {
          setEvents(response.data);
        })
  }

  findAllEventTypes(setEventTypes, limit, offset, categories) {
    RESTClient.get('/system/events/types',
        {limit: limit, offset: offset, categories: categories.join(",")}, function (response) {
          setEventTypes(response.data);
        })
  }

  findAllEventTypesOfOrganization(setEventTypes, organizationId, limit, offset, categories) {
    RESTClient.get('/system/events/types',
        {organization_id: organizationId, limit: limit, offset: offset, categories: categories.join(",")},
        function (response) {
      setEventTypes(response.data);
        })
  }

  getDatabaseSummary(setSummary) {
    RESTClient.get("/system/database", {}, function(response) {
      setSummary(response.data);
    })
  }

}

export default SystemService
