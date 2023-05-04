import RESTClient from '../util/RESTClient'

class AuthenticationManagementService {

  createInitialUser(email, password, name, successCallback, errorCallback) {
    RESTClient.post('/system/authentication/mgmt/initialuser',
        {email: email, password: password, name: name}, successCallback, errorCallback);
  }

  findAllOrganizations(setOrganizations, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations', {limit: limit, offset: offset}, function (response) {
      setOrganizations(response.data)
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

  findAllTenantsOfOrganization(organizationId, setTenants, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants',
        {limit: limit, offset: offset}, function (response) {
      setTenants(response.data)
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

  findAllUsersOfTenant(organizationId, tenantId, setUsers, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users',
        {limit: limit, offset: offset}, function (response) {
      setUsers(response.data)
    });
  }

  findUserOfTenant(organizationId, tenantId, userId, setUser, setIsDeletable = undefined) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users/show/' + userId,
        {}, function (response) {
          setUser(response.data.user)

          if (setIsDeletable) {
            setIsDeletable(response.data.is_deletable);
          }
        });
  }

  editUserOfTenant(organizationId, tenantId, userId, name, email, successCallback, errorCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users/show/' + userId,
        {name: name, email: email}, successCallback, errorCallback);
  }

  editUserOfTenantPassword(organizationId, tenantId, userId, password, successCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users/show/' + userId + '/password',
        {password: password}, successCallback);
  }

  deleteUserOfTenant(organizationId, tenantId, userId, successCallback) {
    RESTClient.delete('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users/show/' + userId,
        successCallback);
  }

  resetMFAOfUserOfTenant(organizationId, tenantId, userId, successCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/users/show/' + userId + '/mfa/reset',
        {}, successCallback);
  }

  findAllSessions(setSessions, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/sessions',
        {limit: limit, offset: offset}, function (response) {
          setSessions(response.data);
    })
  }

  findSessionsOfOrganization(setSessions, organizationId, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/sessions',
        {limit: limit, offset: offset}, function (response) {
          setSessions(response.data);
        })
  }

  findSessionsOfTenant(setSessions, organizationId, tenantId, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/sessions',
        {limit: limit, offset: offset}, function (response) {
          setSessions(response.data);
        })
  }

  invalidateSession(sessionId, successCallback) {
    RESTClient.delete('/system/authentication/mgmt/organizations/sessions/show/' + sessionId, successCallback)
  }

  findAllTapPermissions(organizationId, tenantId, setTaps, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/taps',
        {limit: limit, offset: offset}, function (response) {
      setTaps(response.data);
    })
  }

  findTapPermission(organizationId, tenantId, tapUuid, setTap) {
    RESTClient.get('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/taps/show/' + tapUuid,
        {}, function (response) {
          setTap(response.data);
    })
  }

  createTapPermission(organizationId, tenantId, name, description, successCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/taps',
        {name: name, description: description}, successCallback);
  }

  deleteTapPermission(organizationId, tenantId, tapUuid, successCallback) {
    RESTClient.delete('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/taps/show/' + tapUuid,
        successCallback);
  }

  cycleTapSecret(organizationId, tenantId, tapUuid, successCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/taps/show/' + tapUuid + '/secret/cycle', {},
        successCallback);
  }

  editTapAuthentication(organizationId, tenantId, tapUuid, name, description, successCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/show/' + organizationId + '/tenants/show/' + tenantId + '/taps/show/' + tapUuid,
        {name: name, description: description}, successCallback);
  }

  findAllSuperAdmins(setUsers, limit, offset) {
    RESTClient.get('/system/authentication/mgmt/organizations/superadmins',
        {limit: limit, offset: offset}, function (response) {
      setUsers(response.data);
    })
  }

  findSuperAdmin(userId, setUser, setIsDeletable = undefined) {
    RESTClient.get('/system/authentication/mgmt/organizations/superadmins/show/' + userId,
        {}, function (response) {
          setUser(response.data.user);

          if (setIsDeletable) {
            setIsDeletable(response.data.is_deletable);
          }
        })
  }

  createSuperAdministrator(email, password, name, successCallback, errorCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations/superadmins',
        {email: email, password: password, name: name}, successCallback, errorCallback);
  }

  editSuperAdministrator(userId, name, email, successCallback, errorCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/superadmins/show/' + userId,
        {name: name, email: email}, successCallback, errorCallback);
  }

  editSuperAdministratorPassword(userId, password, successCallback) {
    RESTClient.put('/system/authentication/mgmt/organizations/superadmins/show/' + userId + '/password',
        {password: password}, successCallback);
  }

  deleteSuperAdmin(userId, successCallback) {
    RESTClient.delete('/system/authentication/mgmt/organizations/superadmins/show/' + userId,
        successCallback);
  }

  resetMFAOfSuperAdmin(userId, successCallback) {
    RESTClient.post('/system/authentication/mgmt/organizations/superadmins/show/' + userId + '/mfa/reset',
        {}, successCallback);
  }

}

export default AuthenticationManagementService