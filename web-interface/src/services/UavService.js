import RESTClient from "../util/RESTClient";

export default class UavService {

  findAll(setUavs, timeRange, taps, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/uav/uavs", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setUavs(response.data)
    )
  }

  findOne(setUav, identifier, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/uav/uavs/show/${identifier}`, { taps: tapsList },
      (response) => setUav(response.data)
    )
  }

}