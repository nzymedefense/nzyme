import RESTClient from "../../util/RESTClient";

export default class AssetsService {

  findAllDHCPTransactions(timeRange, taps, limit, offset, setTransactions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dhcp/transactions", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setTransactions(response.data)
    )
  }

  findDHCPTransaction(transactionId, transactionTime, taps, setTransaction) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/ethernet/dhcp/transactions/show/${transactionId}`, { transaction_time: transactionTime, taps: tapsList },
        (response) => setTransaction(response.data)
    )
  }

}