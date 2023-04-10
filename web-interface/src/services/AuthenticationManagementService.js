import RESTClient from '../util/RESTClient'

class AuthenticationManagementService {

  findAllOrganizations(setOrganizations) {
    RESTClient.get('/system/authentication/mgmt/organizations', {}, function (response) {
      setOrganizations(response.data.organizations)
    })
  }

  findOrganization(id, setOrganization) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + id, {}, function (response) {
      setOrganization(response.data)
    })
  }

  createOrganization(name, description, successCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations',
        {name: name, description: description}, successCallback);
  }

  editOrganization(id, name, description, successCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/show/' + id,
        {name: name, description: description}, successCallback);
  }

  deleteOrganization(id, successCallback) {
    RESTClient.delete('/system/authentication/mgmt/organizations/show/' + id, successCallback);
  }

  findTenantsOfOrganization(organizationId, setTenants) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants', {}, function (response) {
      setTenants(response.data.tenants)
    })
  }

  findTenantOfOrganization(organizationId, tenantId, setTenant) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/' + tenantId, {}, function (response) {
      setTenant(response.data)
    })
  }


}

export default AuthenticationManagementService