import RESTClient from '../../util/RESTClient'

export default class SocksService {

  findAllTunnels(timeRange, taps, limit, offset, setTunnels) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/ethernet/socks/tunnels", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setTunnels(response.data)
    )
  }

}