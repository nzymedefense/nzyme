import RESTClient from "../util/RESTClient";

function browserTimeZone() {
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";
  } catch (e) {
    return "UTC";
  }
}

class TimelinesService {

  findAllOfAddress(addressType, address, organizationId, tenantId, timeRange, excludedEventTypes, limit, offset, setEvents) {
    RESTClient.get(`/timelines/show/type/${addressType}/address/${address}`,
      {
        time_range: timeRange,
        excluded_event_types: excludedEventTypes,
        limit: limit,
        offset: offset,
        organization_id: organizationId,
        tenant_id: tenantId,
        time_zone: browserTimeZone()
      },
      function (response) {
        setEvents(response.data);
      })
  }

}

export default TimelinesService;