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

  getSSIDOfBSSIDAdvertisementHistogram(bssid, ssid, minutes, taps, setAdvertisementHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/advertisements/histogram",
        { minutes: minutes, taps: tapsList }, function (response) {
          setAdvertisementHistogram(response.data);
    })
  }

  getSSIDOfBSSIDSignalWaterfall(bssid, ssid, frequency, minutes, taps, setWaterfall) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/frequencies/show/" + frequency + "/signal/waterfall",
        { minutes: minutes, taps: tapsList }, function (response) {
          setWaterfall(response.data);
    })
  }

}

export default Dot11Service
