import RESTClient from '../util/RESTClient'

class LocationsService {

  findAll(organizationId, tenantId, setLocations) {
    RESTClient.get(`/locations/organizations/${organizationId}/tenants/${tenantId}`, {}, (response) => {
      setLocations(response.data)
    })
  }

  findOne(locationId, organizationId, tenantId, setLocation, errorCallback = undefined) {
    RESTClient.get(`/locations/organizations/${organizationId}/tenants/${tenantId}/show/${locationId}`, {}, (response) => {
      setLocation(response.data)
    }, errorCallback)
  }

}

export default LocationsService;