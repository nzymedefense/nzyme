import RESTClient from "../util/RESTClient";

class MonitorsService {

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