const axios = require('axios')

class PingService {
  static buildUri (uri) {
    let stableRoot = window.appConfig.nzymeApiUri
    if (window.appConfig.nzymeApiUri.slice(-1) !== '/') {
      stableRoot = window.appConfig.nzymeApiUri + '/'
    }

    let stableUri = uri
    if (uri.charAt(0) === '/') {
      stableUri = uri.substr(1)
    }

    return stableRoot + stableUri
  }

  ping () {
    // NOT USING RESTClient wrapper here because it's kind of a special call with special error handler etc and we
    // can keep things simple this way.

    const self = this

    axios.get(PingService.buildUri('/ping'))
      .then(function (response) {
        if (response.data === 'pong') {
          self.setState({ apiConnected: true })
        } else {
          self.setState({ apiConnected: false })
        }
      })
      .catch(function () {
        self.setState({ apiConnected: false })
      })
  }
}

export default PingService
