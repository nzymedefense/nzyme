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

  markAlertAsResolved(uuid, successCallback) {
    RESTClient.put('/alerts/show/' + uuid + '/resolve', {}, successCallback);
  }

  findAlertTimeline(uuid, setTimeline, limit, offset) {
    RESTClient.get('/alerts/show/' + uuid + '/timeline', {limit: limit, offset: offset}, function (response) {
      setTimeline(response.data)
    })
  }

}

export default DetectionAlertsService;