import RESTClient from "../util/RESTClient";

class DetectionAlertsService {

  findAllAlerts(setAlerts, limit, offset) {
    RESTClient.get('/alerts', {limit: limit, offset: offset}, function (response) {
      setAlerts(response.data)
    })
  }

  findAlert(uuid, setAlert) {
    RESTClient.get('/alerts/show/' + uuid, {}, function (response) {
      setAlert(response.data)
    })
  }

  deleteAlert(uuid, successCallback) {
    RESTClient.delete('/alerts/show/' + uuid, successCallback);
  }

  deleteAlerts(uuids, successCallback) {
    RESTClient.put('/alerts/many/delete', {uuids: uuids}, successCallback);
  }

  markAlertAsResolved(uuid, successCallback) {
    RESTClient.put('/alerts/show/' + uuid + '/resolve', {}, successCallback);
  }

  markAlertsAsResolved(uuids, successCallback) {
    RESTClient.put('/alerts/many/resolve', {uuids: uuids}, successCallback);
  }

  findAlertTimeline(uuid, setTimeline, limit, offset) {
    RESTClient.get('/alerts/show/' + uuid + '/timeline', {limit: limit, offset: offset}, function (response) {
      setTimeline(response.data)
    })
  }

}

export default DetectionAlertsService;