import RESTClient from "../../util/RESTClient";

export default class L4Service {

  findAllSessions(organizationId, tenantId, taps, filters, timeRange, orderColumn, orderDirection, limit, offset, setSessions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions", {
          organization_id: organizationId,
          tenant_id: tenantId,
          taps: tapsList,
          filters: filters,
          time_range: timeRange,
          order_column: orderColumn,
          order_direction: orderDirection,
          limit: limit,
          offset: offset
        },
        (response) => setSessions(response.data)
    )
  }

  getSessionsStatistics(timeRange, taps, setStatistics) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/statistics", { time_range: timeRange, taps: tapsList },
        (response) => setStatistics(response.data)
    )
  }

  getTopTrafficSources(organizationId, tenantId, taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/sources/traffic/top", {
          organization_id: organizationId,
          tenant_id: tenantId,
          taps: tapsList,
          filters: filters,
          time_range: timeRange,
          limit: limit,
          offset: offset
        },
        (response) => setData(response.data)
    )
  }

  getLeastCommonNonEphemeralDestinationPorts(taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/ports/destination/non-ephemeral/bottom",
        { taps: tapsList, filters: filters, time_range: timeRange, limit: limit, offset: offset },
        (response) => setData(response.data)
    )
  }

}