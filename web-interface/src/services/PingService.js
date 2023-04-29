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

  ping (setApiConnected, setNzymeInformation) {
    // NOT USING RESTClient wrapper here because it's kind of a special call with special error handler etc and we
    // can keep things simple this way.

    axios.get(PingService.buildUri('/ping'))
      .then(function (response) {
        if (response.data) {
          setApiConnected(true);
          setNzymeInformation(response.data);
        } else {
          setNzymeInformation(null);
          setApiConnected(false);
        }
      })
      .catch(function () {
        setNzymeInformation(null);
        setApiConnected(false);
      })
  }
}

export default PingService
