import {API_ROOT} from "./API";
import {notify} from "react-notify-toast";

var axios = require('axios');

const RESTClient = {

  get(uri, params, successCallback) {
    axios.get(API_ROOT + uri, { params: params })
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
    axios.post(API_ROOT + uri, data)
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
    axios.put(API_ROOT + uri, data)
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