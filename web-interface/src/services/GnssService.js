import RESTClient from "../util/RESTClient";

export default class GnssService {

  findGnssTaps(organizationId, tenantId, showAll, setGnssTaps) {
    RESTClient.get(`/gnss/taps`, { organization_id: organizationId, tenant_id: tenantId, show_all: showAll },
      (response) => setGnssTaps(response.data)
    )
  }

  getConstellationCoordinates(constellation, timeRange, tapId, setCoordinates) {
    RESTClient.get(`/gnss/constellations/${constellation}/coordinates`, { time_range: timeRange, tap: tapId },
      (response) => setCoordinates(response.data)
    )
  }

  getPdopHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/pdop/histogram", { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getFixSatellitesHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/fix/satellites/histogram", { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getFixStatusHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/fix/status/histogram", { time_range: timeRange, tap: tapId },
        (response) => setHistogram(response.data)
    )
  }

  getDistances(timeRange, tapId, setDistances) {
    RESTClient.get("/gnss/distances", { time_range: timeRange, tap: tapId },
        (response) => setDistances(response.data)
    )
  }

  getAltitudeHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/altitude/histogram", { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getElevationMask(tapId, setElevationMask) {
    RESTClient.get("/gnss/elevationmask", { tap: tapId },
      (response) => setElevationMask(response.data)
    )
  }

  cleanElevationMask(tapUuid, onSuccess) {
    RESTClient.delete(`/gnss/elevationmask/tap/${tapUuid}`, onSuccess);
  }

  findAllSatellitesInView(timeRange, tapId, setSatellites) {
    RESTClient.get("/gnss/satellites/visible/list", { time_range: timeRange, tap: tapId },
      (response) => setSatellites(response.data)
    )
  }

  getRfMonJammingIndicatorHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/rfmon/jamming-indicator/histogram", { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getRfMonAgcCountHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/rfmon/agc-count/histogram", { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getRfMonNoiseHistogram(timeRange, tapId, setHistogram) {
    RESTClient.get("/gnss/rfmon/noise/histogram", { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getPrnSnoHistogram(constellation, prn, timeRange, tapId, setHistogram) {
    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/sno/histogram`,
      { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getPrnElevationHistogram(constellation, prn, timeRange, tapId, setHistogram) {
    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/elevation/histogram`,
      { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getPrnAzimuthHistogram(constellation, prn, timeRange, tapId, setHistogram) {
    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/azimuth/histogram`,
      { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getPrnDopplerHistogram(constellation, prn, timeRange, tapId, setHistogram) {
    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/doppler/histogram`,
      { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
  }

  getPrnMultipathHistogram(constellation, prn, timeRange, tapId, setHistogram) {
    RESTClient.get(`/gnss/constellations/${constellation}/prns/show/${prn}/multipath/histogram`,
      { time_range: timeRange, tap: tapId },
      (response) => setHistogram(response.data)
    )
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