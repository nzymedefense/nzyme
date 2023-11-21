import RESTClient from '../util/RESTClient'

class ContextService {

  createMacAddressContext(macAddress, subsystem, name, description, notes, organizationId, tenantId, successCallback, errorCallback) {
    RESTClient.post(
        '/context/mac',
        {
          mac_address: macAddress,
          subsystem: subsystem,
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