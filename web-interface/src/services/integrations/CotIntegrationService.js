import RESTClient from "../../util/RESTClient";

export default class CotIntegrationService {

  findAllOutputs(setOutputs, organizationId, tenantId, limit, offset) {
    RESTClient.get(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot`,
        {limit: limit, offset: offset}, (response) => setOutputs(response.data));
  }

  findOutput(setOutput, organizationId, tenantId, outputId) {
    RESTClient.get(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}`,
        {}, (response) => setOutput(response.data));
  }

  createOutput(organizationId, tenantId, name, description, connectionType, tapLeafType, address, port, successCallback, errorCallback) {
    RESTClient.post(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot`,
        {name: name, description: description, connection_type: connectionType, leaf_type_tap: tapLeafType, address: address, port: port}, successCallback, errorCallback);
  }

  editOutput(organizationId, tenantId, outputId, name, description, connectionType, tapLeafType, address, port, successCallback, errorCallback) {
    RESTClient.put(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}`,
        {name: name, description: description, connection_type: connectionType, leaf_type_tap: tapLeafType, address: address, port: port}, successCallback, errorCallback);
  }

  deleteOutput(organizationId, tenantId, outputId, onSuccess) {
    RESTClient.delete(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}`, onSuccess);
  }

  pauseOutput(organizationId, tenantId, outputId, onSuccess) {
    RESTClient.put(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}/pause`, {}, onSuccess);
  }

  startOutput(organizationId, tenantId, outputId, onSuccess) {
    RESTClient.put(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}/start`, {}, onSuccess);
  }

}