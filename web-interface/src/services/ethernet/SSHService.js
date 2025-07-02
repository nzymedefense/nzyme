import RESTClient from '../../util/RESTClient'

export default class SSHService {

  findAllTunnels(organizationId, tenantId, timeRange, taps, limit, offset, setSessions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/ssh/sessions", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setSessions(response.data)
    )
  }

}