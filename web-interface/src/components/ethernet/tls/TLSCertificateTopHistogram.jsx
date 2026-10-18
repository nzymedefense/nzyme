import React, {useContext, useEffect, useState} from "react";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {TapContext} from "../../../App";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import ThreeColumnHistogram from "../../widgets/histograms/ThreeColumnHistogram";
import TLSService from "../../../services/ethernet/TLSService";
import {TLS_CERTIFICATE_FILTER_FIELDS} from "./TLSCertificateFilterFields";

const tlsService = new TLSService();

/*
 * Distribution tables of the certificates page. Column two counts the sessions and probe hops
 * that used a matching certificate, column three the distinct certificates.
 */
const TYPES = {
  issuers: {
    load: (...args) => tlsService.getTopIssuersHistogram(...args),
    titles: ["Issuer", "Uses", "Certificates"],
    filter: {field: "issuer"}
  },
  key_types: {
    load: (...args) => tlsService.getKeyTypesHistogram(...args),
    titles: ["Key Type", "Uses", "Certificates"],
    filter: null
  },
  signature_algorithms: {
    load: (...args) => tlsService.getSignatureAlgorithmsHistogram(...args),
    titles: ["Signature Algorithm", "Uses", "Certificates"],
    filter: {field: "signature_algorithm"}
  }
}

export default function TLSCertificateTopHistogram({type, timeRange, filters, setFilters, revision}) {

  const config = TYPES[type];

  const [organizationId, tenantId] = useSelectedTenant();

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [histogram, setHistogram] = useState(null);

  const [orderColumn, setOrderColumn] = useState("value1");
  const [orderDirection, setOrderDirection] = useState("DESC");

  useEffect(() => {
    setHistogram(null);

    config.load(
      setHistogram, organizationId, tenantId, timeRange, orderColumn, orderDirection, limit, 0, filters, selectedTaps
    );
  }, [config, selectedTaps, organizationId, tenantId, limit, timeRange, filters, orderColumn, orderDirection, revision]);

  if (!histogram) {
    return <LoadingSpinner />
  }

  if (histogram.total === 0) {
    return (
      <div className="alert alert-info mb-0 mt-2">
        No certificates recorded.
      </div>
    )
  }

  return <ThreeColumnHistogram data={histogram}
                               columnTitles={config.titles}
                               columnFilterElements={[
                                 config.filter ? {...config.filter, fields: TLS_CERTIFICATE_FILTER_FIELDS, setFilters: setFilters} : null,
                                 null, null
                               ]}
                               orderColumn={orderColumn}
                               setOrderColumn={setOrderColumn}
                               orderDirection={orderDirection}
                               setOrderDirection={setOrderDirection}
                               orderColumnOneIsKey={true}
                               limit={limit}
                               setLimit={setLimit} />

}
