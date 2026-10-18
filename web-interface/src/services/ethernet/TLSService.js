import RESTClient from '../../util/RESTClient'

export default class TLSService {

  findAllSessions(organizationId, tenantId, timeRange, filters, orderColumn, orderDirection, taps, limit, offset, setSessions) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/tls/sessions", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, filters: filters, order_column: orderColumn, order_direction: orderDirection, taps: tapsList, limit: limit, offset: offset },
        (response) => setSessions(response.data)
    )
  }

  findSession(sessionKey, organizationId, tenantId, taps, setSession) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/ethernet/tls/sessions/show/${sessionKey}`, { organization_id: organizationId, tenant_id: tenantId, taps: tapsList },
        (response) => setSession(response.data)
    )
  }

  findCertificate(sha256, organizationId, tenantId, timeRange, taps, setCertificate) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(`/ethernet/tls/certificates/show/${sha256}`, { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, taps: tapsList },
        (response) => setCertificate(response.data)
    )
  }

  getActiveSessionCountHistogram(setHistogram, timeRange, filters, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/tls/sessions/active/histogram", {
        filters: filters,
        time_range: timeRange,
        taps: tapsList,
      }, (response) => setHistogram(response.data)
    )
  }

  getTopHistogram(path, setHistogram, organizationId, tenantId, timeRange, orderColumn, orderDirection, limit, offset, filters, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get(path, {
        organization_id: organizationId,
        tenant_id: tenantId,
        filters: filters,
        time_range: timeRange,
        taps: tapsList,
        order_column: orderColumn,
        order_direction: orderDirection,
        limit: limit,
        offset: offset
      }, (response) => setHistogram(response.data)
    )
  }

  getTopClientsHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/sessions/clients/top/histogram", ...args)
  }

  getTopServersHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/sessions/servers/top/histogram", ...args)
  }

  getTopServerNamesHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/sessions/servernames/top/histogram", ...args)
  }

  getTopServerPortsHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/sessions/ports/top/histogram", ...args)
  }

  getVersionsHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/sessions/versions/histogram", ...args)
  }

  getTopJa4Histogram(...args) {
    this.getTopHistogram("/ethernet/tls/sessions/ja4/top/histogram", ...args)
  }

  findAllCertificates(organizationId, tenantId, timeRange, filters, orderColumn, orderDirection, taps, limit, offset, setCertificates) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/tls/certificates", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, filters: filters, order_column: orderColumn, order_direction: orderDirection, taps: tapsList, limit: limit, offset: offset },
        (response) => setCertificates(response.data)
    )
  }

  findExpiringCertificates(organizationId, tenantId, timeRange, filters, days, orderColumn, orderDirection, taps, limit, offset, setCertificates) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/tls/certificates/expiring", { organization_id: organizationId, tenant_id: tenantId, time_range: timeRange, filters: filters, days: days, order_column: orderColumn, order_direction: orderDirection, taps: tapsList, limit: limit, offset: offset },
        (response) => setCertificates(response.data)
    )
  }

  getNewCertificatesHistogram(setHistogram, timeRange, filters, taps) {
    const tapsList = Array.isArray(taps) ? taps.join(",") : (taps === "*" ? "*" : null)

    RESTClient.get("/ethernet/tls/certificates/new/histogram", {
        filters: filters,
        time_range: timeRange,
        taps: tapsList,
      }, (response) => setHistogram(response.data)
    )
  }

  getTopIssuersHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/certificates/issuers/top/histogram", ...args)
  }

  getKeyTypesHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/certificates/keytypes/histogram", ...args)
  }

  getSignatureAlgorithmsHistogram(...args) {
    this.getTopHistogram("/ethernet/tls/certificates/signaturealgorithms/histogram", ...args)
  }

}
