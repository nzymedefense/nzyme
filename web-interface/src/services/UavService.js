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

  classifyUav(identifier, organizationId, tenantId, classification, onSuccess, onFailure) {
    RESTClient.put(`/uav/uavs/organization/${organizationId}/tenant/${tenantId}/show/${identifier}/classify/${classification}`, {}, onSuccess, onFailure);
  }

}