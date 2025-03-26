import RESTClient from "../../util/RESTClient";

export default class CotIntegrationService {

  findAllOutputs(setOutputs, organizationId, tenantId, limit, offset) {
    RESTClient.get(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot`,
        {limit: limit, offset: offset}, (response) => setOutputs(response.data));
  }

  createOutput(organizationId, tenantId, name, description, tapLeafType, address, port, successCallback, errorCallback) {
    RESTClient.post(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot`,
        {name: name, description: description, leaf_type_tap: tapLeafType, address: address, port: port}, successCallback, errorCallback);
  }

}