import {API_ROOT} from "./API";
import {notify} from "react-notify-toast";

var axios = require('axios');

const RESTClient = {

  buildUri(uri) {
    let stableRoot = API_ROOT;
    if(API_ROOT.slice(-1) !== '/') {
      stableRoot = API_ROOT + "/";
    }

    let stableUri = uri;
    if (uri.charAt(0) === "/") {
      stableUri = uri.substr(1);
    }

    return stableRoot + stableUri;
  },

  get(uri, params, successCallback) {
    axios.get(this.buildUri(uri), { params: params })
      .then(function (response) {
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

  post(uri, data, successCallback) {
    axios.post(this.buildUri(uri), data)
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
    axios.put(this.buildUri(uri), data)
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