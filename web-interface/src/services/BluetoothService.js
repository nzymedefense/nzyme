import RESTClient from '../util/RESTClient'

class BluetoothService {

  findAllDevices(setDevices, timeRange, taps, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/bluetooth/devices", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setDevices(response.data)
    )
  }

}

export default BluetoothService;