import RESTClient from '../util/RESTClient'

class AlertsService {
  findActive (limit) {
    const self = this

    RESTClient.get('/alerts/active', { limit: limit }, function (response) {
      self.setState({ active_alerts: response.data.alerts })
    })
  }

  findAll (page) {
    const self = this

    RESTClient.get('/alerts', { page: page }, function (response) {
      self.setState({ alerts: response.data.alerts, total_alerts: response.data.total })
    })
  }

  findActiveCount () {
    const self = this

    RESTClient.get('/alerts/active', { limit: 9999 }, function (response) {
      self.setState({ active_alerts_count: response.data.alerts.length })
    })
  }

  findOne (id, hook) {
    RESTClient.get('/alerts/show/' + id, {}, function (response) {
      hook(response.data)
    })
  }

  getConfiguration () {
    const self = this

    RESTClient.get('/alerts/configuration', {}, function (response) {
      self.setState({ alert_configuration: response.data })
    })
  }
}

export default AlertsService
