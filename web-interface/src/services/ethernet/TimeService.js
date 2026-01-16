import RESTClient from '../../util/RESTClient'

export default class TimeService {

  findAllNTPTransactions(organizationId, tenantId, timeRange, filters, orderColumn, orderDirection, taps, limit, offset, setTransactions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/time/ntp/transactions", {
        organization_id: organizationId,
        tenant_id: tenantId,
        time_range: timeRange,
        filters: filters,
        order_column: orderColumn,
        order_direction: orderDirection,
        taps: tapsList,
        limit: limit,
        offset: offset
      },
      (response) => setTransactions(response.data)
    )
  }

  getNTPTransactionsHistogram(timeRange, filters, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/time/ntp/transactions/histogram", {
      time_range: timeRange,
        filters: filters,
        taps: tapsList
      },
      (response) => setHistogram(response.data)
    )
  }

  getNTPClientRequestResponseRatioHistogram(organizationId, tenantId, timeRange, filters, taps, limit, offset, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/time/ntp/clients/requestresponseratio/histogram", {
        organization_id: organizationId,
        tenant_id: tenantId,
        limit: limit,
        offset: offset,
        time_range: timeRange,
        filters: filters,
        taps: tapsList
      },
      (response) => setHistogram(response.data)
    )
  }

  getNTPTopServersHistogram(organizationId, tenantId, timeRange, filters, taps, limit, offset, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/time/ntp/servers/top/histogram", {
        organization_id: organizationId,
        tenant_id: tenantId,
        limit: limit,
        offset: offset,
        time_range: timeRange,
        filters: filters,
        taps: tapsList
      },
      (response) => setHistogram(response.data)
    )
  }

}