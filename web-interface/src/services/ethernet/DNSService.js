import RESTClient from '../../util/RESTClient'

export default class DNSService {

  findDNSStatistics (timeRange, taps, setStatistics) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get('/ethernet/dns/statistics', { time_range: timeRange, taps: tapsList },
        function (response) {
          setStatistics(response.data)
        })
  }

}