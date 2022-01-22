import RESTClient from '../util/RESTClient'

class DashboardService {
  findAll () {
    const self = this

    RESTClient.get('/dashboard', {}, function (response) {
      self.setState({ dashboard: response.data })
    })
  }
}

export default DashboardService
