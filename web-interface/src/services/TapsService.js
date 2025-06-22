import RESTClient from '../util/RESTClient'

class TapService {

  findAllTapsHighLevel(organizationId, tenantId, successCallback) {
    RESTClient.get('/taps/highlevel', { organization_id: organizationId, tenant_id: tenantId }, successCallback);
  }

  findAllTaps(organizationId, tenantId, setTaps) {
    RESTClient.get('/taps', { organization_id: organizationId, tenant_id: tenantId }, function (response) {
      setTaps(response.data.taps)
    })
  }

  findTap(uuid, setTap) {
    RESTClient.get('/taps/show/' + uuid, {}, function (response) {
      setTap(response.data)
    })
  }

  findMetricsOfTap(uuid, setTapMetrics) {
    RESTClient.get('/taps/show/' + uuid + '/metrics', {}, function (response) {
      setTapMetrics(response.data)
    })
  }

  findGaugeMetricHistogramOfTap(uuid, metricName, setTapMetric) {
    RESTClient.get('/taps/show/' + uuid + '/metrics/gauges/' + metricName + '/histogram', {}, function (response) {
      setTapMetric(response.data)
    })
  }

  findTimerMetricHistogramOfTap(uuid, metricName, setTapMetric) {
    RESTClient.get('/taps/show/' + uuid + '/metrics/timers/' + metricName + '/histogram', {}, function (response) {
      setTapMetric(response.data)
    })
  }

}

export default TapService
