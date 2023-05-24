import RESTClient from '../util/RESTClient'

class EthernetService {

  findDNSStatistics (hours, taps, setStatistics) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get('/ethernet/dns/statistics', { hours: hours, taps: tapsList },
        function (response) {
      setStatistics(response.data)
    })
  }

}

export default EthernetService
