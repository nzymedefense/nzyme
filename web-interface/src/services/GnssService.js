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

  getTimeDeviationHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/time/deviation/histogram", { time_range: timeRange, taps: tapsList },
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

  findAllSatellitesInView(timeRange, taps, setSatellites) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/satellites/visible/list", { time_range: timeRange, taps: tapsList },
      (response) => setSatellites(response.data)
    )
  }

  getPrnSnrHistogram(constellation, prn, timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/snr/histogram`,
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

}