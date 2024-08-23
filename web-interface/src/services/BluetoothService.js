import RESTClient from '../util/RESTClient'

class BluetoothService {

  findAllDevices(setDevices, timeRange, taps, limit, offset) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get("/bluetooth/devices", { time_range: timeRange, taps: tapsList, limit: limit, offset: offset },
        (response) => setDevices(response.data)
    )
  }

  findOneDevice(setDevice, mac, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/bluetooth/devices/show/${mac}`, { taps: tapsList },
        (response) => setDevice(response.data)
    )
  }

  getRssiHistogramOfDevice(setData, mac, timeRange, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/bluetooth/devices/show/${mac}/rssi/histogram`, { time_range: timeRange, taps: tapsList },
        (response) => setData(response.data)
    )
  }

  getRssiOfDeviceByTap(setData, mac, timeRange, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : "*";

    RESTClient.get(`/bluetooth/devices/show/${mac}/rssi/bytap`, { time_range: timeRange, taps: tapsList },
        (response) => setData(response.data)
    )
  }

}

export default BluetoothService;