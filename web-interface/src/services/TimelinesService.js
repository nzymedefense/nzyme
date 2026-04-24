import RESTClient from "../util/RESTClient";

class TimelinesService {

  findAllOfAddress(addressType, address, organizationId, tenantId, timeRange, limit, offset, setEvents) {
    RESTClient.get(`/timelines/show/type/${addressType}/address/${address}`,
      { time_range: timeRange, limit: limit, offset: offset, organization_id: organizationId, tenant_id: tenantId },
      function (response) {
        setEvents(response.data);
      })
  }

}

export default TimelinesService;