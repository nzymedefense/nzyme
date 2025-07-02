import RESTClient from '../../util/RESTClient'

export default class SocksService {

  findAllTunnels(organizationId, tenantId, timeRange, taps, limit, offset, setTunnels) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/socks/tunnels", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setTunnels(response.data)
    )
  }

}