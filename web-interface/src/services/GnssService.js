import RESTClient from "../util/RESTClient";

export default class GnssService {

  getConstellationCoordinates(constellation, timeRange, taps, setCoordinates) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/gnss/constellations/${constellation}/coordinates`, { time_range: timeRange, taps: tapsList },
      (response) => setCoordinates(response.data)
    )
  }

  getPdopHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/pdop/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getFixSatellitesHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/fix/satellites/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getFixStatusHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/fix/status/histogram", { time_range: timeRange, taps: tapsList },
        (response) => setHistogram(response.data)
    )
  }

  getDistances(timeRange, taps, setDistances) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/distances", { time_range: timeRange, taps: tapsList },
        (response) => setDistances(response.data)
    )
  }

  getAltitudeHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/altitude/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getSatellitesInViewHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/satellites/visible/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getElevationMask(taps, setElevationMask) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/elevationmask", { taps: tapsList },
      (response) => setElevationMask(response.data)
    )
  }

  cleanElevationMask(tapUuid, onSuccess) {
    RESTClient.delete(`/gnss/elevationmask/tap/${tapUuid}`, onSuccess);
  }

  findAllSatellitesInView(timeRange, taps, setSatellites) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/satellites/visible/list", { time_range: timeRange, taps: tapsList },
      (response) => setSatellites(response.data)
    )
  }

  getRfMonJammingIndicatorHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/rfmon/jamming-indicator/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getRfMonAgcCountHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/rfmon/agc-count/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getRfMonNoiseHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/rfmon/noise/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getPrnSnoHistogram(constellation, prn, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/sno/histogram`,
      { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getPrnElevationHistogram(constellation, prn, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/elevation/histogram`,
      { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getPrnAzimuthHistogram(constellation, prn, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/azimuth/histogram`,
      { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getPrnDopplerHistogram(constellation, prn, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/doppler/histogram`,
      { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  findMonitoringRule(uuid, organizationId, tenantId, setRule) {
    RESTClient.get(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/rules/show/${uuid}`,
        {}, (response) => setRule(response.data)
    )
  }

  findAllMonitoringRules(organizationId, tenantId, limit, offset, setRules) {
    RESTClient.get(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/rules`,
        { limit: limit, offset: offset }, (response) => setRules(response.data)
    )
  }

  createMonitoringRule(name, description, conditions, taps, organizationId, tenantId, onSuccess, onFailure) {
    RESTClient.post(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/rules`,
        { name: name, description: description, conditions: conditions, taps: taps }, onSuccess, onFailure
    )
  }

  editMonitoringRule(uuid, name, description, conditions, taps, organizationId, tenantId, onSuccess, onFailure) {
    RESTClient.put(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/rules/show/${uuid}`,
      { name: name, description: description, conditions: conditions, taps: taps }, onSuccess, onFailure
    )
  }

  deleteMonitoringRule(uuid, organizationId, tenantId, onSuccess) {
    RESTClient.delete(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/rules/show/${uuid}`,
      onSuccess)
  }

  getMonitoringConfiguration(organizationId, tenantId, setSettings) {
    RESTClient.get(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/configuration`,
        {}, (response) => setSettings(response.data)
    )
  }

  updateMonitoringConfiguration(newConfig, organizationId, tenantId, successCallback, errorCallback) {
    RESTClient.put(`/gnss/monitoring/organization/${organizationId}/tenant/${tenantId}/configuration`,
        { change: newConfig }, successCallback, errorCallback)
  }

}