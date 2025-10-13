import RESTClient from '../../util/RESTClient'

export default class SSHService {

  findAllTunnels(organizationId, tenantId, timeRange, filters, orderColumn, orderDirection, taps, limit, offset, setSessions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/ssh/sessions", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, filters: filters, order_column: orderColumn, order_direction: orderDirection, taps: tapsList, limit: limit, offset: offset },
        (response) => setSessions(response.data)
    )
  }

  findSession(sessionId, organizationId, tenantId, taps, setSession) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/ethernet/ssh/sessions/show/${sessionId}`, { organization_id: organizationId, tenant_id: tenantId, taps: tapsList },
        (response) => setSession(response.data)
    )
  }

}