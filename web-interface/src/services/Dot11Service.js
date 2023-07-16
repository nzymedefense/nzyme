import RESTClient from '../util/RESTClient'

class Dot11Service {

  findAllBSSIDs(minutes, taps, setBSSIDs) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids", { minutes: minutes, taps: tapsList },
        function (response) {
          setBSSIDs(response.data.bssids)
    })
  }

  findSSIDsOfBSSID(bssid, minutes, taps, successCallback) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids", { minutes: minutes, taps: tapsList },
        function (response) {
          successCallback(response.data.ssids);
    })
  }

  getBSSIDAndSSIDHistogram(minutes, taps, setBSSIDAndSSIDHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/histogram", { minutes: minutes, taps: tapsList },
        function (response) {
          setBSSIDAndSSIDHistogram(response.data)
    })
  }

  findSSIDOfBSSID(bssid, ssid, minutes, taps, setSSID) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid,
        { minutes: minutes, taps: tapsList }, function (response) {
          setSSID(response.data);
    })
  }

  getSSIDOfBSSIDAdvertisementHistogram(bssid, ssid, minutes, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/advertisements/histogram",
        { minutes: minutes, taps: tapsList }, function (response) {
          setHistogram(response.data);
    })
  }

  getSSIDOfBSSIDActiveChannelHistogram(bssid, ssid, minutes, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/frequencies/histogram",
        { minutes: minutes, taps: tapsList }, function (response) {
          setHistogram(response.data);
    })
  }

  getSSIDOfBSSIDSignalWaterfall(bssid, ssid, frequency, minutes, taps, setWaterfall) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/frequencies/show/" + frequency + "/signal/waterfall",
        { minutes: minutes, taps: tapsList }, function (response) {
          setWaterfall(response.data);
    })
  }

  findAllConnectedClients(minutes, taps, setConnectedClients, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/connected",
        { minutes: minutes, taps: tapsList, limit: limit, offset: offset },
        function (response) {
          setConnectedClients(response.data)
        })
  }

  findAllDisconnectedClients(minutes, taps, setDisconnectedClients, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/disconnected",
        { minutes: minutes, taps: tapsList, limit: limit, offset: offset },
        function (response) {
          setDisconnectedClients(response.data)
    })
  }

}

export default Dot11Service
