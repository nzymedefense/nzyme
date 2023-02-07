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

  getTapSecret (setTapSecret) {
    RESTClient.get('/taps/secret', {}, function (response) {
      setTapSecret(response.data.secret)
    })
  }

  cycleTapSecret (setTapSecret) {
    RESTClient.post('/taps/secret/cycle', {}, function (response) {
      setTapSecret(response.data.secret)
      notify.show('Tap secret has been cycled. You must now update it in the configuration of all your taps.', 'success')
    })
  }
}

export default TapService
