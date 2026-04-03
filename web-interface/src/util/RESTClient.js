import Store from './Store'
import {toast} from "react-toastify";

const axios = require('axios')

const RESTClient = {

  getAuthHeaders () {
    const headers = {}

    // Add API token authorization if we have one.
    if (Store.get('sessionid') !== undefined) {
      headers.Authorization = 'Bearer ' + Store.get('sessionid')
    }

    return headers
  },

  buildUri (uri) {
    let stableRoot = window.appConfig.nzymeApiUri
    if (window.appConfig.nzymeApiUri.slice(-1) !== '/') {
      stableRoot = window.appConfig.nzymeApiUri + '/'
    }

    let stableUri = uri
    if (uri.charAt(0) === '/') {
      stableUri = uri.substr(1)
    }

    return stableRoot + stableUri
  },

  get (uri, params, successCallback, errorCallback = undefined) {
    axios.get(this.buildUri(uri), { params: params, headers: this.getAuthHeaders() })
      .then(function (response) {
        successCallback(response)
      })
      .catch(function (error) {
        if (error.response) {
          if (errorCallback) {
            errorCallback(error)
          } else {
            toast.error('REST call failed. (HTTP ' + error.response.status + ')')
          }
        } else {
          console.log("REST call error: " + error);
          toast.error('REST call failed. No response. Is nzyme running?')
        }
      })
  },

  post (uri, data, successCallback, errorCallback = undefined) {
    axios.post(this.buildUri(uri), data, { headers: this.getAuthHeaders() })
      .then(function (response) {
        successCallback(response)
      })
      .catch(function (error) {
        if (errorCallback) {
          errorCallback(error)
          return;
        }

        if (error.response) {
          if (error.response.status === 422) {
            toast.error('Could not create entity. Quota exceeded. Please contact your administrator.')
          } else {
            toast.error('REST call failed. (HTTP ' + error.response.status + ')')
          }
        } else {
          toast.error('REST call failed. No response. Is nzyme running?')
        }
      })
  },

  postMultipart (uri, formData, standardErrorHandling, successCallback, errorCallback = undefined) {
    const headers = this.getAuthHeaders();
    headers["Content-Type"] = "multipart/form-data";

    axios.post(this.buildUri(uri), formData, { headers: headers })
      .then(function (response) {
        successCallback(response)
      })
      .catch(function (error) {
        if (errorCallback) {
          errorCallback(error)
        }

        if (standardErrorHandling) {
          if (error.response) {
            toast.error('REST call failed. (HTTP ' + error.response.status + ')')
          } else {
            toast.error('REST call failed. No response. Is nzyme running?')
          }
        }
      })
  },

  put (uri, data, successCallback, errorCallback = undefined) {
    axios.put(this.buildUri(uri), data, { headers: this.getAuthHeaders() })
      .then(function (response) {
        successCallback(response)
      })
      .catch(function (error) {
        if (errorCallback) {
          errorCallback(error)
        } else {
          if (error.response) {
            toast.error('REST call failed. (HTTP ' + error.response.status + ')')
          } else {
            toast.error('REST call failed. No response. Is nzyme running?')
          }
        }
      })
  },

  delete(uri, successCallback, errorCallback = undefined) {
    axios.delete(this.buildUri(uri), { headers: this.getAuthHeaders() })
      .then(function (response) {
        successCallback(response)
      })
      .catch(function (error) {
        if (errorCallback) {
          errorCallback(error)
        } else {
          if (error.response) {
            toast.error('REST call failed. (HTTP ' + error.response.status + ')')
          } else {
            toast.error('REST call failed. No response. Is nzyme running?')
          }
        }
      })
  }

}

export default RESTClient
