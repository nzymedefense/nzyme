import RESTClient from "../util/RESTClient";

export default class ConnectService {

  getConfiguration(setConfiguration) {
    RESTClient.get('/system/connect/configuration', {}, function (response) {
      setConfiguration(response.data)
    })
  }

  updateConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/connect/configuration', { change: newConfig }, successCallback, errorCallback)
  }

}