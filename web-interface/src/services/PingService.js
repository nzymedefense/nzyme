import RESTClient from "../util/RESTClient";

const axios = require('axios')

class PingService {

  ping (setApiConnected, setNzymeInformation, successCallback, errorCallback) {
    // NOT USING RESTClient wrapper here because it's kind of a special call with special error handler etc and we
    // can keep things simple this way.

    axios.get(RESTClient.buildUri('/ping'))
      .then(function (response) {
        if (response.data) {
          setApiConnected(true);
          setNzymeInformation(response.data);

          successCallback();
        } else {
          setNzymeInformation(null);
          setApiConnected(false);
        }
      })
      .catch(function () {
        setNzymeInformation(null);
        setApiConnected(false);
        errorCallback();
      })
  }
}

export default PingService
