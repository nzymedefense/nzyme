import RESTClient from '../../util/RESTClient'

export default class DNSService {

  findDNSStatistics (timeRange, taps, setStatistics) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get('/ethernet/dns/statistics', { time_range: timeRange, taps: tapsList },
        (response) => setStatistics(response.data)
    )
  }

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

}