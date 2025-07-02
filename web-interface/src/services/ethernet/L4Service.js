import RESTClient from "../../util/RESTClient";

export default class L4Service {

  findAllSessions(organizationId, tenantId, taps, filters, timeRange, orderColumn, orderDirection, limit, offset, setSessions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/l4/sessions", {
          organizationId: organizationId,
          tenantId: tenantId,
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

}