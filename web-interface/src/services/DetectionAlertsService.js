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

}

export default DetectionAlertsService;