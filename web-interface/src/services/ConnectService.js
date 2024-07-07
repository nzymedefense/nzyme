import RESTClient from "../util/RESTClient";

export default class ConnectService {

  getStatus(setStatus) {
    RESTClient.get('/system/connect/status', {}, function (response) {
      setStatus(response.data)
    })
  }

  getConfiguration(setConfiguration) {
    RESTClient.get('/system/connect/configuration', {}, function (response) {
      setConfiguration(response.data)
    })
  }

  updateConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/connect/configuration', { change: newConfig }, successCallback, errorCallback)
  }

}