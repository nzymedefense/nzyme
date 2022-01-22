import RESTClient from '../util/RESTClient'

class ProbesService {
  findAll () {
    const self = this
    RESTClient.get('/system/probes', {}, function (response) {
      self.setState({ probes: response.data.probes })
    })
  }

  findAllTraps () {
    const self = this
    RESTClient.get('/system/probes/traps', {}, function (response) {
      self.setState({ traps: response.data.traps })
    })
  }
}

export default ProbesService
