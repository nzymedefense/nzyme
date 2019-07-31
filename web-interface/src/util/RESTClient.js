import {notify} from "react-notify-toast";
import Store from "./Store";

var axios = require('axios');

const RESTClient = {

  getAuthHeaders() {
    const headers = {};

    // Add API token authorization if we have one.
    if (Store.get("api_token") !== undefined) {
      headers["Authorization"] = "Bearer " + Store.get("api_token");
    }

    return headers;
  },

  buildUri(uri) {
    let stableRoot = window.appConfig.nzymeApiUri;
    if(window.appConfig.nzymeApiUri.slice(-1) !== '/') {
      stableRoot = window.appConfig.nzymeApiUri + "/";
    }

    let stableUri = uri;
    if (uri.charAt(0) === "/") {
      stableUri = uri.substr(1);
    }

    return stableRoot + stableUri;
  },

  get(uri, params, successCallback) {
    axios.get(this.buildUri(uri), { params: params, headers: this.getAuthHeaders() })
      .then(function (response) {
        successCallback(response);
      })
      .catch(function (error) {
        if (error.response) {
          notify.show("REST call failed. (HTTP " + error.response.status + ")", "error");
        } else {
          notify.show("REST call failed. No response. Is nzyme running?", "error");
        }
      });
  },

  post(uri, data, successCallback) {
    axios.post(this.buildUri(uri), data, { headers: this.getAuthHeaders() })
      .then(function(response) {
        successCallback(response);
      })
      .catch(function (error) {
        console.log(error);
        if (error.response) {
          notify.show("REST call failed. (HTTP " + error.response.status + ")", "error");
        } else {
          notify.show("REST call failed. No response. Is nzyme running?", "error");
        }
      });
  },

  put(uri, data, successCallback) {
    axios.put(this.buildUri(uri), data, { headers: this.getAuthHeaders() })
      .then(function(response) {
        successCallback(response);
      })
      .catch(function (error) {
        console.log(error);
        if (error.response) {
          notify.show("REST call failed. (HTTP " + error.response.status + ")", "error");
        } else {
          notify.show("REST call failed. No response. Is nzyme running?", "error");
        }
      });
  },

};

export default RESTClient;