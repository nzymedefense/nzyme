import RESTClient from "../util/RESTClient";

export default class UavService {

  findAll(setUavs, organizationId, tenantId, timeRange, taps, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}`,
        { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setUavs(response.data)
    )
  }

  findOne(setUav, organizationId, tenantId, identifier, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${identifier}`, { taps: tapsList },
      (response) => setUav(response.data)
    )
  }

  findTimeline(setTimeline, organizationId, tenantId, timeRange, identifier, limit, offset) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${identifier}/timelines`,
        { time_range: timeRange, limit: limit, offset: offset },
        (response) => setTimeline(response.data)
    )
  }

  findTimelineVectors(setTrack, uavIdentifier, timelineId, organizationId, tenantId, onComplete) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${uavIdentifier}/timelines/show/${timelineId}`, { },
        (response) => { setTrack(response.data); if (onComplete) onComplete(response.data) }
    )
  }

  classifyUav(identifier, organizationId, tenantId, classification, onSuccess, onFailure) {
    RESTClient.put(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${identifier}/classify/${classification}`, {}, onSuccess, onFailure);
  }

  findAllCustomTypes(setTypes, organizationId, tenantId, limit, offset) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/custom`,
      {limit: limit, offset: offset },
      (response) => setTypes(response.data)
    )
  }

  findCustomType(setType, uuid, organizationId, tenantId) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/custom/show/${uuid}`, {},
        (response) => setType(response.data)
    )
  }

  createCustomType(organizationId, tenantId, matchType, matchValue, defaultClassification, type, model, name, onSuccess, onFailure) {
    RESTClient.post(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/custom`,
        { match_type: matchType, match_value: matchValue, default_classification: defaultClassification, type: type, model: model, name: name },
        onSuccess, onFailure
    )
  }

  editCustomType(uuid, organizationId, tenantId, matchType, matchValue, defaultClassification, type, model, name, onSuccess, onFailure) {
    RESTClient.put(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/custom/show/${uuid}`,
        { match_type: matchType, match_value: matchValue, default_classification: defaultClassification, type: type, model: model, name: name },
        onSuccess, onFailure
    )
  }

  deleteCustomType(uuid, organizationId, tenantId, onSuccess) {
    RESTClient.delete(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/custom/show/${uuid}`, onSuccess);
  }

  findAllConnectTypes(setTypes, organizationId, tenantId, limit, offset) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/connect`,
      {limit: limit, offset: offset },
      (response) => setTypes(response.data)
    )
  }

  getMonitoringConfig(setConfig, organizationId, tenantId) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/monitoring`, {},
        (response) => setConfig(response.data)
    )
  }

  setMonitoringConfig(alertOnUnknown, alertOnFriendly, alertOnNeutral, alertOnHostile, organizationId, tenantId, successCallback) {
    RESTClient.put(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/monitoring`,
        { alert_on_unknown: alertOnUnknown,
          alert_on_friendly: alertOnFriendly,
          alert_on_neutral: alertOnNeutral,
          alert_on_hostile: alertOnHostile
        }, successCallback
    )
  }

}