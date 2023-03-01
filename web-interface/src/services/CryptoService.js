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

  testTLSCertificate(nodeId, formData, successCallback, errorCallback) {
    RESTClient.postMultipart("/system/crypto/tls/node/" + nodeId + "/test", formData, false,
      function(response) {
        successCallback(response)
      }, function(error) {
        errorCallback(error.response);
      });
  }

}

export default CryptoService
