import RESTClient from '../util/RESTClient'

class ContextService {

  findAllMacAddressContext(organizationId, tenantId, setContext, limit, offset) {
    RESTClient.get(
        "/context/mac/organization/show/" + organizationId + "/tenant/show/" + tenantId,
        {limit: limit, offset: offset},
        (response) => setContext(response.data)
    )
  }

  createMacAddressContext(macAddress, name, description, notes, organizationId, tenantId, successCallback, errorCallback) {
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
        successCallback,
        errorCallback
    );
  }

}

export default ContextService;