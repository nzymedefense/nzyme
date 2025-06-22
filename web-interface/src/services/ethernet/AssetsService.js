import RESTClient from "../../util/RESTClient";

export default class AssetsService {

  findAllAssets(organizationId, tenantId, timeRange, orderColumn, orderDirection, limit, offset, setAssets) {
    RESTClient.get("/ethernet/assets", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, order_column: orderColumn, order_direction: orderDirection, limit: limit, offset: offset },
        (response) => setAssets(response.data)
    )
  }

  findAsset(uuid, organizationId, tenantId, setAsset) {
    RESTClient.get(`/ethernet/assets/show/${uuid}`, { organization_id: organizationId, tenant_id: tenantId },
        (response) => setAsset(response.data)
    )
  }

  findAllDHCPTransactions(timeRange, orderColumn, orderDirection, taps, limit, offset, setTransactions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/dhcp/transactions", { time_range: timeRange, order_column: orderColumn, order_direction: orderDirection, taps: tapsList, limit: limit, offset: offset },
        (response) => setTransactions(response.data)
    )
  }

  findDHCPTransaction(transactionId, transactionTime, taps, setTransaction) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/ethernet/dhcp/transactions/show/${transactionId}`, { transaction_time: transactionTime, taps: tapsList },
        (response) => setTransaction(response.data)
    )
  }

}