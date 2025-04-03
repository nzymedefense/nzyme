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

  createOutput(organizationId, tenantId, formData, successCallback, errorCallback) {
    RESTClient.postMultipart(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot`,
        formData, true, successCallback, errorCallback);
  }

  editOutput(organizationId, tenantId, outputId, formData, successCallback, errorCallback) {
    RESTClient.postMultipart(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}/update`,
        formData, true, successCallback, errorCallback);
  }

  editOutputCertificate(organizationId, tenantId, outputId, formData, successCallback, errorCallback) {
    RESTClient.postMultipart(`/system/authentication/mgmt/organizations/show/${organizationId}/tenants/show/${tenantId}/integrations/cot/show/${outputId}/certificate/update`,
        formData, true, successCallback, errorCallback);
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