import RESTClient from '../util/RESTClient'

class CryptoService {
  getPGPSummary (setKeys, setMetrics) {
    RESTClient.get('/system/crypto/summary', {}, function (response) {
      setKeys(Object.values(response.data.pgp_keys))
      setMetrics(response.data.metrics)
    })
  }
}

export default CryptoService
