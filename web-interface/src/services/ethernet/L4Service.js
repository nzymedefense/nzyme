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

  getTopTrafficSourceMacs(organizationId, tenantId, taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/sources/traffic/macs/top", {
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

  getTopTrafficSourceAddresses(organizationId, tenantId, taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/sources/traffic/addresses/top", {
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

  getTopTrafficDestinationMacs(organizationId, tenantId, taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/destinations/traffic/macs/top", {
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

  getTopTrafficDestinationAddresses(organizationId, tenantId, taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/destinations/traffic/addresses/top", {
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

  getTopDestinationPorts(taps, filters, timeRange, limit, offset, setData) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions/histograms/ports/destination/all/top",
        { taps: tapsList, filters: filters, time_range: timeRange, limit: limit, offset: offset },
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