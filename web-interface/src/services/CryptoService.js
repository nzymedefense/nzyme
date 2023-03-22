import RESTClient from '../util/RESTClient'

class CryptoService {

  getPGPSummary(setCrypto, setMetrics) {
    RESTClient.get('/system/crypto/summary', {}, function (response) {
      setCrypto(response.data)
      setMetrics(response.data.metrics)
    })
  }

  findTLSCertificateOfNode(nodeId, setCertificate) {
    RESTClient.get('/system/crypto/tls/node/' + nodeId, {}, function (response) {
      setCertificate(response.data)
    })
  }

  regenerateSelfSignedTLSCertificate(nodeId, successCallback) {
    RESTClient.put('/system/crypto/tls/node/' + nodeId + '/regenerate', {}, successCallback);
  }

  installIndividualTLSCertificate(nodeId, formData, successCallback) {
    RESTClient.postMultipart('/system/crypto/tls/node/' + nodeId, formData, true, successCallback);
  }

  findTLSWildcardCertificate(certId, setCertificate) {
    RESTClient.get('/system/crypto/tls/wildcard/' + certId, {}, function (response) {
      setCertificate(response.data)
    })
  }

  installWildcardTLSCertificate(formData, successCallback) {
    RESTClient.postMultipart('/system/crypto/tls/wildcard', formData, true, successCallback);
  }

  replaceWildcardTLSCertificate(certificateId, formData, successCallback) {
    RESTClient.postMultipart('/system/crypto/tls/wildcard/' + certificateId + '/replace', formData, true, successCallback);
  }

  testTLSWildcardCertificateNodeMatcher(regex, successCallback) {
    RESTClient.get('/system/crypto/tls/wildcard/nodematchertest/', {regex: regex}, successCallback)
  }

  updateTLSWildcardCertificateNodeMatcher(certId, regex, successCallback) {
    RESTClient.put('/system/crypto/tls/wildcard/' + certId + '/node_matcher', {node_matcher: regex}, successCallback)
  }

  deleteTLSWildcardCertificate(certId, successCallback) {
    RESTClient.delete('/system/crypto/tls/wildcard/' + certId, successCallback)
  }

  testTLSCertificate(formData, successCallback, errorCallback) {
    RESTClient.postMultipart("/system/crypto/tls/test", formData, false,
      function(response) {
        successCallback(response)
      }, function(error) {
        errorCallback(error.response);
      });
  }

  updatePGPConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/crypto/pgp/configuration', { change: newConfig }, successCallback, errorCallback)
  }
}

export default CryptoService
