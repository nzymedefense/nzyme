import RESTClient from '../util/RESTClient'

class TapService {
  findAllTaps (setTaps) {
    RESTClient.get('/taps', {}, function (response) {
      setTaps(response.data.taps)
    })
  }

  findTap (uuid, setTap) {
    RESTClient.get('/taps/show/' + uuid, {}, function (response) {
      setTap(response.data)
    })
  }

  findMetricsOfTap (uuid, setTapMetrics) {
    RESTClient.get('/taps/show/' + uuid + '/metrics', {}, function (response) {
      setTapMetrics(response.data)
    })
  }

  findGaugeMetricHistogramOfTap (uuid, metricName, setTapMetric) {
    RESTClient.get('/taps/show/' + uuid + '/metrics/gauges/' + metricName + '/histogram', {}, function (response) {
      setTapMetric(response.data)
    })
  }

}

export default TapService
