import RESTClient from '../util/RESTClient'

class Dot11Service {

  findAllSSIDNames(setSSIDs) {
    RESTClient.get("/dot11/networks/ssids/names", {}, function (response) {
      setSSIDs(response.data)
    })
  }

  findBSSID(bssid, taps, setBSSID) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid, { taps: tapsList },
        function (response) {
          setBSSID(response.data)
    })
  }

  getBSSIDAdvertisementHistogram(bssid, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/advertisements/histogram",
        { time_range: timeRange, taps: tapsList }, function (response) {
          setHistogram(response.data);
        })
  }

  getBSSIDActiveChannelHistogram(bssid, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/frequencies/histogram",
        { time_range: timeRange, taps: tapsList }, function (response) {
          setHistogram(response.data);
        })
  }

  getBSSIDSignalWaterfall(bssid, timeRange, taps, setWaterfall) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/signal/waterfall",
        { time_range: timeRange, taps: tapsList }, function (response) {
          setWaterfall(response.data);
        })
  }

  findAllBSSIDs(timeRange, taps, setBSSIDs) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids", { time_range: timeRange, taps: tapsList },
        function (response) {
          setBSSIDs(response.data.bssids)
    })
  }

  findSSIDsOfBSSID(bssid, timeRange, taps, successCallback) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids", { time_range: timeRange, taps: tapsList },
        function (response) {
          successCallback(response.data.ssids);
    })
  }

  getBSSIDAndSSIDHistogram(timeRange, taps, setBSSIDAndSSIDHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/histogram", { time_range: timeRange, taps: tapsList },
        function (response) {
          setBSSIDAndSSIDHistogram(response.data)
    })
  }

  findSSIDOfBSSID(bssid, ssid, timeRange, taps, setSSID) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid,
        { time_range: timeRange, taps: tapsList }, function (response) {
          setSSID(response.data);
    })
  }

  getSSIDOfBSSIDAdvertisementHistogram(bssid, ssid, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/advertisements/histogram",
        { time_range: timeRange, taps: tapsList }, function (response) {
          setHistogram(response.data);
    })
  }

  getSSIDOfBSSIDActiveChannelHistogram(bssid, ssid, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/frequencies/histogram",
        { time_range: timeRange, taps: tapsList }, function (response) {
          setHistogram(response.data);
    })
  }

  getSSIDOfBSSIDSignalWaterfall(bssid, ssid, frequency, timeRange, taps, setWaterfall) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/networks/bssids/show/" + bssid + "/ssids/show/" + ssid + "/frequencies/show/" + frequency + "/signal/waterfall",
        { time_range: timeRange, taps: tapsList }, function (response) {
          setWaterfall(response.data);
    })
  }

  findConnectedClients(timeRange, taps, setClients, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/connected",
        { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        function (response) {
          setClients(response.data);
        })
  }

  findDisconnectedClients(timeRange, skipRandomized, taps, setClients, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/disconnected",
        { skip_randomized: skipRandomized, time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        function (response) {
          setClients(response.data);
        })
  }

  getConnectedClientsHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/connected/histogram", { time_range: timeRange, taps: tapsList },
        function (response) { setHistogram(response.data); })
  }

  getDisconnectedClientsHistogram(timeRange, skipRandomized, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/dot11/clients/disconnected/histogram",
        { skip_randomized: skipRandomized, time_range: timeRange, taps: tapsList },
        function (response) { setHistogram(response.data); })
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

  getMonitoredNetworkImportData(ssidUUID, setImportData) {
      RESTClient.get("/dot11/monitoring/ssids/show/" + ssidUUID + "/import/data", {}, function (response) {
        setImportData(response.data);
      })
  }

  writeMonitoredNetworkImportData(ssidUUID, bssids, channels, securitySuites, successCallback) {
    RESTClient.post("/dot11/monitoring/ssids/show/" + ssidUUID + "/import/data",
        {bssids: bssids, channels: channels, security_suites: securitySuites}, successCallback);
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

  getDiscoHistogram(discoType, timeRange, taps, bssids, monitoredNetworkId, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";
    const bssidList = bssids ? bssids.join(",") : null
    const monitoredNetworkIdParam = monitoredNetworkId ? monitoredNetworkId : null;

    RESTClient.get("/dot11/disco/histogram",
        { disco_type: discoType, time_range: timeRange, taps: tapsList, bssids: bssidList, monitored_network_id: monitoredNetworkIdParam },
        function (response) {
          setHistogram(response.data)
    })
  }

  getDiscoTopSenders(timeRange, taps, monitoredNetworkId, limit, offset, setTopSenders) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";
    const monitoredNetworkIdParam = monitoredNetworkId ? monitoredNetworkId : null;

    RESTClient.get("/dot11/disco/lists/senders",
        { time_range: timeRange, taps: tapsList, monitored_network_id: monitoredNetworkIdParam, limit: limit, offset: offset},
        function (response) {
          setTopSenders(response.data)
    })
  }

  getDiscoTopReceivers(timeRange, taps, monitoredNetworkId, limit, offset, setTopReceivers) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";
    const monitoredNetworkIdParam = monitoredNetworkId ? monitoredNetworkId : null;

    RESTClient.get("/dot11/disco/lists/receivers",
        { time_range: timeRange, taps: tapsList, monitored_network_id: monitoredNetworkIdParam, limit: limit, offset: offset},
        function (response) {
          setTopReceivers(response.data)
        })
  }

  getDiscoTopPairs(timeRange, taps, monitoredNetworkId, bssids, limit, offset, setTopPairs) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";
    const monitoredNetworkIdParam = monitoredNetworkId ? monitoredNetworkId : null;
    const bssidsParam = bssids ? bssids.join(",") : null;

    RESTClient.get("/dot11/disco/lists/pairs",
        { time_range: timeRange, taps: tapsList, monitored_network_id: monitoredNetworkIdParam, bssids: bssidsParam, limit: limit, offset: offset},
        function (response) {
          setTopPairs(response.data)
        })
  }

  getDiscoDetectionConfiguration(monitoredNetworkUUID, setConfiguration) {
    RESTClient.get("/dot11/disco/config/detection", {monitored_network_id: monitoredNetworkUUID}, function (response) {
          setConfiguration(response.data);
    })
  }

  setDiscoDetectionConfiguration(methodType, configuration, monitoredNetworkUUID, successCallback) {
    RESTClient.put("/dot11/disco/config/detection",
        {monitored_network_id: monitoredNetworkUUID, method_type: methodType, configuration: configuration}, successCallback);
  }

  simulateDiscoDetectionConfiguration(methodType, configuration, monitoredNetworkUUID, tapId, setAnomalies) {
    RESTClient.post("/dot11/disco/config/detection/simulate",
        {
          monitored_network_id: monitoredNetworkUUID,
          tap_id: tapId,
          method_type: methodType,
          configuration: configuration
        }, function (response) {
      setAnomalies(response.data);
    })
  }

  simulateSimilarSSIDs(monitoredNetworkUUID, threshold, successCallback) {
    RESTClient.get("/dot11/monitoring/ssids/show/" + monitoredNetworkUUID + "/configuration/similarssids/simulate", {threshold: threshold}, successCallback)
  }

  setSimilarSSIDMonitorConfiguration(monitoredNetworkUUID, threshold, successCallback) {
    RESTClient.put("/dot11/monitoring/ssids/show/" + monitoredNetworkUUID + "/configuration/similarssids", {threshold: threshold}, successCallback)
  }

  addRestrictedSSIDSubstring(monitoredNetworkUUID, substring, successCallback) {
    RESTClient.post("/dot11/monitoring/ssids/show/" + monitoredNetworkUUID + "/configuration/restricted-ssid-substrings",
        {substring: substring}, successCallback)
  }

  deleteRestrictedSSIDSubstring(monitoredNetworkUUID, substringUUID, successCallback) {
    RESTClient.delete("/dot11/monitoring/ssids/show/" + monitoredNetworkUUID + "/configuration/restricted-ssid-substrings/show/" + substringUUID,
        successCallback)
  }

  findBSSIDLocation(bssid, locationUuid, floorUuid, timeRange, setResult, setErrorMessage) { // Floor UUID is optional.
    RESTClient.get("/dot11/locations/locate/bssid/" + bssid, { time_range: timeRange, location_uuid: locationUuid, floor_uuid: floorUuid},
        (response) => {
          setErrorMessage(null);
          setResult(response.data)
        }, (error) => {
          if (error.response && error.response.status === 400 && error.response.data.message) {
            setErrorMessage(error.response.data.message);
          } else {
            setErrorMessage("Unexpected error.");
          }
        })
  }

  findClientLocation(mac, locationUuid, floorUuid, timeRange, setResult, setErrorMessage) { // Floor UUID is optional.
    RESTClient.get("/dot11/locations/locate/client/" + mac, { time_range: timeRange, location_uuid: locationUuid, floor_uuid: floorUuid},
        (response) => {
          setErrorMessage(null);
          setResult(response.data)
        }, (error) => {
          if (error.response && error.response.status === 400 && error.response.data.message) {
            setErrorMessage(error.response.data.message);
          } else {
            setErrorMessage("Unexpected error.");
          }
        })
  }

  findAllFloorsOfLocation(locationId, setFloors, limit, offset) {
    RESTClient.get("/dot11/locations/show/" + locationId + "/floors",
        {limit: limit, offset: offset}, (response) => {
          setFloors(response.data);
        })
  }

  findAllMonitoredProbeRequests(organizationUUID, tenantUUID, limit, offset, setProbeRequests) {
    RESTClient.get(
        "/dot11/monitoring/proberequests",
        {limit: limit, offset: offset, organization_uuid: organizationUUID, tenant_uuid: tenantUUID},
        (response) => setProbeRequests(response.data)
    )
  }

  findMonitoredProbeRequest(organizationUUID, tenantUUID, uuid, setProbeRequest) {
    RESTClient.get(
        `/dot11/monitoring/proberequests/show/${uuid}`,
        {organization_uuid: organizationUUID, tenant_uuid: tenantUUID},
        (response) => setProbeRequest(response.data)
    )
  }

  createMonitoredProbeRequest(organizationUUID, tenantUUID, ssid, notes, onSuccess) {
    RESTClient.post(
        "/dot11/monitoring/proberequests",
        {organization_id: organizationUUID, tenant_id: tenantUUID, ssid: ssid, notes: notes},
        onSuccess
    )
  }

  updateMonitoredProbeRequest(organizationUUID, tenantUUID, uuid, ssid, notes, onSuccess) {
    RESTClient.put(
        `/dot11/monitoring/proberequests/show/${uuid}`,
        {organization_id: organizationUUID, tenant_id: tenantUUID, ssid: ssid, notes: notes},
        onSuccess
    )
  }

  deleteMonitoredProbeRequest(uuid, onSuccess) {
    RESTClient.delete(`/dot11/monitoring/proberequests/show/${uuid}`, onSuccess)
  }

}

export default Dot11Service
