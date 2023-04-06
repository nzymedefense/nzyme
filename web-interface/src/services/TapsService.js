import RESTClient from '../util/RESTClient'
import { notify } from 'react-notify-toast'

class TapService {
  findAllTaps (setTaps) {
    RESTClient.get('/taps', {}, function (response) {
      setTaps(response.data.taps)
    })
  }

  findTap (tapName, setTap) {
    RESTClient.get('/taps/show/' + tapName, {}, function (response) {
      setTap(response.data)
    })
  }

  deleteTap (tapName, successCallback) {
    RESTClient.delete('/taps/show/' + tapName, {}, successCallback)
  }

  findMetricsOfTap (tapName, setTapMetrics) {
    RESTClient.get('/taps/show/' + tapName + '/metrics', {}, function (response) {
      setTapMetrics(response.data)
    })
  }

  findGaugeMetricHistogramOfTap (tapName, metricName, setTapMetric) {
    RESTClient.get('/taps/show/' + tapName + '/metrics/gauges/' + metricName + '/histogram', {}, function (response) {
      setTapMetric(response.data)
    })
  }

}

export default TapService
