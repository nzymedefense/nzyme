import RESTClient from '../util/RESTClient'

class CryptoService {
  getPGPSummary (setCrypto, setMetrics) {
    RESTClient.get('/system/crypto/summary', {}, function (response) {
      setCrypto(response.data)
      setMetrics(response.data.metrics)
    })
  }
}

export default CryptoService
