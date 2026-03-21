import RESTClient from "../util/RESTClient";

class MonitorsService {

  findAllOfType(monitorType, organizationId, tenantId, limit, offset, setMonitors) {
    RESTClient.get(`/monitors/type/${monitorType}`,
      { limit: limit, offset: offset, organization_id: organizationId, tenant_id: tenantId, },
      function (response) {
        setMonitors(response.data);
      })
  }


  createMonitor(monitorType, name, description, taps, trigger_condition, interval, filters, organizationId, tenantId, successCallback, errorCallback) {
    RESTClient.post(`/monitors/type/${monitorType}`,{
      name: name,
      description: description,
      taps: taps,
      trigger_condition: trigger_condition,
      interval: interval,
      filters: JSON.stringify(filters),
      organization_id: organizationId,
      tenant_id: tenantId,
    }, successCallback, errorCallback);
  }

}

export default MonitorsService;