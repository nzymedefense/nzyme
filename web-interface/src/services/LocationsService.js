import RESTClient from '../util/RESTClient'

class LocationsService {

  findAll(organizationId, tenantId, setLocations) {
    RESTClient.get(`/locations/organizations/${organizationId}/tenants/${tenantId}`, {}, (response) => {
      setLocations(response.data)
    })
  }

}

export default LocationsService;