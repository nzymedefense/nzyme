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

  findAllTenantsOfOrganization(organizationId, setTenants) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants', {}, function (response) {
      setTenants(response.data.tenants)
    })
  }

  findTenantOfOrganization(organizationId, tenantId, setTenant) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId, {}, function (response) {
      setTenant(response.data)
    })
  }

  createTenantOfOrganization(organizationId, name, description, successCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants',
        {name: name, description: description}, successCallback);
  }

  editTenantOfOrganization(organizationId, tenantId, name, description, successCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId,
        {name: name, description: description}, successCallback);
  }

  deleteTenantOfOrganization(organizationId, tenantId, successCallback) {
    RESTClient.delete('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId,
        successCallback);
  }

  createUserOfTenant(organizationId, tenantId, email, password, name, successCallback, errorCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users',
        {email: email, password: password, name: name}, successCallback, errorCallback);
  }

}

export default AuthenticationManagementService