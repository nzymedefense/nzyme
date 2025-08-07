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

  getAltitudeHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/altitude/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

  getSatellitesInViewHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/satellites/visibie/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

}