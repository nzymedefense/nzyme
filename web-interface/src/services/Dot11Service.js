import RESTClient from '../util/RESTClient'

class Dot11Service {

  findAllBSSIDs(minutes, taps, setBSSIDs) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get('/dot11/networks/bssids', { minutes: minutes, taps: tapsList },
        function (response) {
          setBSSIDs(response.data.bssids)
    })
  }

}

export default Dot11Service
