import RESTClient from '../util/RESTClient'

class Dot11Service {

  findAllSSIDNames(setSSIDs) {
    RESTClient.get("/dot11/networks/ssids/names", {}, function (response) {
      setSSIDs(response.data)
    })
  }

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

  findAllClients(minutes, taps, setConnectedClients, setDisconnectedClients, connectedLimit, connectedOffset, disconnectedLimit, disconnectedOffset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients",
        { minutes: minutes, taps: tapsList, connectedLimit: connectedLimit, connectedOffset: connectedOffset,
          disconnectedLimit: disconnectedLimit, disconnectedOffset: disconnectedOffset },
        function (response) {
          setConnectedClients(response.data.connected);
          setDisconnectedClients(response.data.disconnected);
    })
  }

  getClientHistograms(taps, setHistograms) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/histograms", { taps: tapsList }, function (response) {
          setHistograms(response.data);
    })
  }

  findMergedConnectedOrDisconnectedClient(clientMac, taps, setClient) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/show/" + clientMac, { taps: tapsList }, function (response) {
      setClient(response.data);
    })
  }

  findAllMonitoredSSIDs(setMonitoredSSIDs) {
    RESTClient.get("/dot11/monitoring/ssids", {}, function (response) {
      setMonitoredSSIDs(response.data.ssids);
    })
  }

  findMonitoredSSID(uuid, setMonitoredSSID, successCallback, errorCallback = undefined) {
    RESTClient.get("/dot11/monitoring/ssids/show/" + uuid, {}, function (response) {
      setMonitoredSSID(response.data);
      successCallback();
    }, errorCallback)
  }

  createMonitoredSSID(ssid, tenantId, organizationId, successCallback, errorCallback) {
    RESTClient.post("/dot11/monitoring/ssids",
        {ssid: ssid, tenant_id: tenantId, organization_id: organizationId}, successCallback, errorCallback);
  }

  deleteMonitoredSSID(uuid, successCallback) {
    RESTClient.delete("/dot11/monitoring/ssids/show/" + uuid, successCallback);
  }

  createMonitoredBSSID(ssidUUID, bssid, successCallback, errorCallback) {
    RESTClient.post("/dot11/monitoring/ssids/show/" + ssidUUID + "/bssids",
        {bssid: bssid}, successCallback, errorCallback);
  }

  deleteMonitoredBSSID(ssidUUID, bssidUUID, successCallback) {
    RESTClient.delete("/dot11/monitoring/ssids/show/" + ssidUUID + "/bssids/show/" + bssidUUID, successCallback);
  }

  createMonitoredBSSIDFingerprint(ssidUUID, bssidUUID, fingerprint, successCallback, errorCallback) {
    RESTClient.post("/dot11/monitoring/ssids/show/" + ssidUUID + "/bssids/show/" + bssidUUID + "/fingerprints",
        {fingerprint: fingerprint}, successCallback, errorCallback);
  }

  deleteMonitoredBSSIDFingerprint(ssidUUID, bssidUUID, fingerprintUUID, successCallback) {
    RESTClient.delete("/dot11/monitoring/ssids/show/" + ssidUUID + "/bssids/show/" + bssidUUID + "/fingerprints/show/" + fingerprintUUID, successCallback);
  }

  createMonitoredChannel(ssidUUID, frequency, successCallback, errorCallback) {
    RESTClient.post("/dot11/monitoring/ssids/show/" + ssidUUID + "/channels",
        {frequency: frequency}, successCallback, errorCallback);
  }

  deleteMonitoredChannel(ssidUUID, channelUUID, successCallback) {
    RESTClient.delete("/dot11/monitoring/ssids/show/" + ssidUUID + "/channels/show/" + channelUUID, successCallback);
  }

  createMonitoredSecuritySuite(ssidUUID, suite, successCallback, errorCallback) {
    RESTClient.post("/dot11/monitoring/ssids/show/" + ssidUUID + "/securitysuites",
        {suite: suite}, successCallback, errorCallback);
  }

  deleteMonitoredSecuritySuite(ssidUUID, suiteUUID, successCallback) {
    RESTClient.delete("/dot11/monitoring/ssids/show/" + ssidUUID + "/securitysuites/show/" + suiteUUID, successCallback);
  }

  enableMonitoredNetwork(ssidUUID, successCallback, errorCallback) {
    RESTClient.put("/dot11/monitoring/ssids/show/" + ssidUUID + "/enable", {}, successCallback, errorCallback);
  }

  disableMonitoredNetwork(ssidUUID, successCallback, errorCallback) {
    RESTClient.put("/dot11/monitoring/ssids/show/" + ssidUUID + "/disable", {}, successCallback, errorCallback);
  }

  setMonitoredNetworkAlertEnabledStatus(ssidUUID, alert, status, successCallback) {
    RESTClient.put("/dot11/monitoring/ssids/show/" + ssidUUID + "/alertenabledstatus/" + alert + "/set/" + status,
        {}, successCallback);
  }

  findBuiltinBandits(setBandits) {
    RESTClient.get("/dot11/bandits/builtin", {}, function (response) {
      setBandits(response.data);
    })
  }

  findBuiltinBandit(id, setBandit) {
    RESTClient.get("/dot11/bandits/builtin/show/" + id, {}, function (response) {
      setBandit(response.data);
    })
  }

  findCustomBandits(organizationUUID, tenantUUID, limit, offset, setBandits) {
    RESTClient.get("/dot11/bandits/custom",
        {limit: limit, offset: offset, organization_uuid: organizationUUID, tenant_uuid: tenantUUID},
        function (response) {
      setBandits(response.data);
    })
  }

  findCustomBandit(banditId, setBandits) {
    RESTClient.get("/dot11/bandits/custom/show/" + banditId, {}, function (response) {
      setBandits(response.data);
    })
  }

  createCustomBandit(organizationUUID, tenantUUID, name, description, successCallback) {
    RESTClient.post("/dot11/bandits/custom",
        { organization_id: organizationUUID, tenant_id: tenantUUID, name: name, description: description },
        successCallback);
  }

  editCustomBandit(banditUUID, name, description, successCallback) {
    RESTClient.put("/dot11/bandits/custom/show/" + banditUUID,
        { name: name, description: description }, successCallback);
  }

  deleteCustomBandit(banditUUID, successCallback) {
    RESTClient.delete("/dot11/bandits/custom/show/" + banditUUID, successCallback);
  }

  addFingerprintToCustomBandit(banditUUID, fingerprint, successCallback) {
    RESTClient.post("/dot11/bandits/custom/show/" + banditUUID + "/fingerprints",
        {fingerprint: fingerprint}, successCallback);
  }

  deleteFingerprintOfCustomBandit(banditUUID, fingerprint, successCallback) {
    RESTClient.delete("/dot11/bandits/custom/show/" + banditUUID + "/fingerprints/show/" + fingerprint,
        successCallback);
  }

  updateTrackDetectorConfig(bssid, ssid, frequency, tapUUID, frameThreshold, gapThreshold, signalCenterlineJitter, successCallback) {
    RESTClient.put("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/frequencies/show/" + frequency + "/signal/trackdetector/configuration",
        {
          tap_id: tapUUID,
          frame_threshold: frameThreshold,
          gap_threshold: gapThreshold,
          signal_centerline_jitter: signalCenterlineJitter
        },
        successCallback);
  }

}

export default Dot11Service
