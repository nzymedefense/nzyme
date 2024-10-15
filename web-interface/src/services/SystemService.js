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

  updateRetentionTimes(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/database/retention', { change: newConfig }, successCallback, errorCallback)
  }

  getSidebarTitle(setSidebarTitle, setSidebarSubtitle) {
    RESTClient.get('/system/lookandfeel/sidebartitle', {}, function (response) {
      setSidebarTitle(response.data.title);
      setSidebarSubtitle(response.data.subtitle);
    })
  }

  setSidebarTitle(sidebarTitle, sidebarSubtitle, onSuccess, onError) {
    RESTClient.put('/system/lookandfeel/sidebartitle', {title: sidebarTitle, subtitle: sidebarSubtitle}, onSuccess, onError);
  }

  uploadLoginImage(formData, successCallback, errorCallback) {
    RESTClient.postMultipart('/system/lookandfeel/loginimage', formData, false, successCallback, errorCallback);
  }

  resetLoginImage(successCallback) {
    RESTClient.delete('/system/lookandfeel/loginimage', successCallback);
  }

  getSubsystemsConfiguration(setConfiguration) {
    RESTClient.get(
      "/system/subsystems/configuration",
      {},
      (response) => setConfiguration(response.data)
    )
  }

  updateSubsystemsConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put("/system/subsystems/configuration", { change: newConfig }, successCallback, errorCallback)
  }

}

export default SystemService
