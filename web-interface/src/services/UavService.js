import RESTClient from "../util/RESTClient";

export default class UavService {

  findAll(setUavs, organizationId, tenantId, timeRange, taps, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}`,
        { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setUavs(response.data)
    )
  }

  findOne(setUav, organizationId, tenantId, identifier, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${identifier}`, { taps: tapsList },
      (response) => setUav(response.data)
    )
  }

  findTimeline(setTimeline, organizationId, tenantId, timeRange, identifier, taps, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${identifier}/timelines`,
        { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setTimeline(response.data)
    )
  }

  findTimelineVectors(setTrack, uavIdentifier, timelineId, organizationId, tenantId, taps, onComplete) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${uavIdentifier}/timelines/show/${timelineId}`,
        { taps: tapsList },
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

  createCustomType(organizationId, tenantId, matchType, matchValue, defaultClassification, type, model, name, onSuccess, onFailure) {
    RESTClient.post(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/custom`,
        { match_type: matchType, match_value: matchValue, default_classification: defaultClassification, type: type, model: model, name: name },
        onSuccess, onFailure
    )
  }

  findAllBuiltInTypes(setTypes, organizationId, tenantId, limit, offset) {
    RESTClient.get(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/types/builtin`,
      {limit: limit, offset: offset },
      (response) => setTypes(response.data)
    )
  }

}