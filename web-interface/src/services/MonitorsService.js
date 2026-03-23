import RESTClient from "../util/RESTClient";

class MonitorsService {

  findOne(id, organizationId, tenantId, setMonitor) {
    RESTClient.get(`/monitors/show/${id}`,
      { organization_id: organizationId, tenant_id: tenantId },
      function (response) {
        setMonitor(response.data);
      })
  }


  findAllOfType(monitorType, organizationId, tenantId, limit, offset, setMonitors) {
    RESTClient.get(`/monitors/type/${monitorType}`,
      { limit: limit, offset: offset, organization_id: organizationId, tenant_id: tenantId },
      function (response) {
        setMonitors(response.data);
      })
  }

  createMonitor(monitorType, name, description, taps, trigger_condition, interval, filters, organizationId, tenantId, successCallback, errorCallback) {
    const tapsParam = (taps && taps === "*") ? null : taps;

    RESTClient.post(`/monitors/type/${monitorType}`,{
      name: name,
      description: description,
      taps: tapsParam,
      trigger_condition: trigger_condition,
      interval: interval,
      filters: JSON.stringify(filters),
      organization_id: organizationId,
      tenant_id: tenantId,
    }, successCallback, errorCallback);
  }

}

export default MonitorsService;