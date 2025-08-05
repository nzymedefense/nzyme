import RESTClient from "../util/RESTClient";

export default class GnssService {

  getTimeDeviationHistogram(timeRange, taps, setHistogram) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/gnss/time/deviation/histogram", { time_range: timeRange, taps: tapsList },
      (response) => setHistogram(response.data)
    )
  }

}