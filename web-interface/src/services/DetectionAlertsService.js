import RESTClient from "../util/RESTClient";

class DetectionAlertsService {

  findAllAlerts(setAlerts) {
    RESTClient.get('/alerts', {}, function (response) {
      setAlerts(response.data)
    })
  }

}

export default DetectionAlertsService;