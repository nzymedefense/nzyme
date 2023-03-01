import { notify } from 'react-notify-toast'
import Store from './Store'

const axios = require('axios')

const RESTClient = {

  getAuthHeaders () {
    const headers = {}

    // Add API token authorization if we have one.
    if (Store.get('api_token') !== undefined) {
      headers.Authorization = 'Bearer ' + Store.get('api_token')
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
        console.log(error)
        if (error.response) {
          if (error.response.status === 401) {
            Store.delete('api_token')
          }

          if (error.response.status !== 401) {
            if (errorCallback) {
              errorCallback(error)
            } else {
              notify.show('REST call failed. (HTTP ' + error.response.status + ')', 'error')
            }
          }
        } else {
          notify.show('REST call failed. No response. Is nzyme running?', 'error')
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
        }

        if (error.response) {
          notify.show('REST call failed. (HTTP ' + error.response.status + ')', 'error')
        } else {
          notify.show('REST call failed. No response. Is nzyme running?', 'error')
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
            notify.show('REST call failed. (HTTP ' + error.response.status + ')', 'error')
          } else {
            notify.show('REST call failed. No response. Is nzyme running?', 'error')
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
            notify.show('REST call failed. (HTTP ' + error.response.status + ')', 'error')
          } else {
            notify.show('REST call failed. No response. Is nzyme running?', 'error')
          }
        }
      })
  },

  delete (uri, successCallback, errorCallback = undefined) {
    axios.delete(this.buildUri(uri), { headers: this.getAuthHeaders() })
      .then(function (response) {
        successCallback(response)
      })
      .catch(function (error) {
        if (errorCallback) {
          errorCallback(error)
        } else {
          if (error.response) {
            notify.show('REST call failed. (HTTP ' + error.response.status + ')', 'error')
          } else {
            notify.show('REST call failed. No response. Is nzyme running?', 'error')
          }
        }
      })
  }

}

export default RESTClient
