import RESTClient from '../../util/RESTClient'

export default class DNSService {

  getGlobalStatisticsValue(timeRange, taps, type, setValue) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dns/global/statistics/" + type, { time_range: timeRange, taps: tapsList },
        (response) => setValue(response.data.value)
    )
  }

  getGlobalChart(timeRange, taps, type, setValue) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dns/global/charts/" + type, {time_range: timeRange, taps: tapsList},
        (response) => setValue(response.data)
    )
  }

  getGlobalPairs(timeRange, taps, limit, offset, setPairs) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dns/global/pairs", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setPairs(response.data)
    )
  }

  getGlobalEntropyLog(timeRange, taps, limit, offset, setEntropyLog) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dns/global/entropylog", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setEntropyLog(response.data)
    )
  }

  findAllTransactions(timeRange, taps, limit, offset, setTransactions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dns/transactions/log", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setTransactions(response.data)
    )
  }

  getTransactionCountChart(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/dns/transactions/charts/count", { time_range: timeRange, taps: tapsList },
        (response) => setHistogram(response.data)
    )
  }

}