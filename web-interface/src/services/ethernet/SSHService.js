import RESTClient from '../../util/RESTClient'

export default class SSHService {

  findAllTunnels(timeRange, taps, limit, offset, setSessions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/ssh/sessions", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setSessions(response.data)
    )
  }

}