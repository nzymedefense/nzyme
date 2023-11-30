import RESTClient from '../util/RESTClient'

class ContextService {

  findMacAddressContext(macAddress, setContext, errorCallback) {
    RESTClient.get("/context/mac/show/" + macAddress, {}, (response) => setContext(response.data), errorCallback)
  }

  findAllMacAddressContext(organizationId, tenantId, setContext, limit, offset) {
    RESTClient.get(
        "/context/mac/organization/show/" + organizationId + "/tenant/show/" + tenantId,
        {limit: limit, offset: offset},
        (response) => setContext(response.data)
    )
  }

  createMacAddressContext(macAddress, name, description, notes, organizationId, tenantId, successCallback) {
    RESTClient.post(
        "/context/mac",
        {
          mac_address: macAddress,
          name: name,
          description: description,
          notes: notes,
          organization_id: organizationId,
          tenant_id: tenantId
        },
        successCallback
    );
  }

}

export default ContextService;