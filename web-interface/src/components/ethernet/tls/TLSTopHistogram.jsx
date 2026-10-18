import React, {useContext, useEffect, useState} from "react";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {TapContext} from "../../../App";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import ThreeColumnHistogram from "../../widgets/histograms/ThreeColumnHistogram";
import TLSService from "../../../services/ethernet/TLSService";
import {TLS_SESSION_FILTER_FIELDS} from "./TLSSessionFilterFields";

const tlsService = new TLSService();

/*
 * One component for all top-N tables of the TLS sessions page. The type selects the endpoint,
 * the column titles and which filter a click on the key adds.
 */
const TYPES = {
  clients: {
    load: (...args) => tlsService.getTopClientsHistogram(...args),
    titles: ["Client", "Sessions", "Bytes Exchanged"],
    filter: {field: "client_address", valueSubField: "address"}
  },
  servers: {
    load: (...args) => tlsService.getTopServersHistogram(...args),
    titles: ["Server", "Sessions", "Bytes Exchanged"],
    filter: {field: "server_address", valueSubField: "address"}
  },
  server_names: {
    load: (...args) => tlsService.getTopServerNamesHistogram(...args),
    titles: ["Server Name", "Sessions", "Bytes Exchanged"],
    filter: {field: "server_name"}
  },
  server_ports: {
    load: (...args) => tlsService.getTopServerPortsHistogram(...args),
    titles: ["Server Port", "Sessions", "Bytes Exchanged"],
    filter: {field: "server_port"}
  },
  versions: {
    load: (...args) => tlsService.getVersionsHistogram(...args),
    titles: ["Version", "Sessions", "Bytes Exchanged"],
    filter: {field: "version"}
  }
}

export default function TLSTopHistogram({type, timeRange, filters, setFilters, revision}) {

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
        No TLS sessions recorded.
      </div>
    )
  }

  return <ThreeColumnHistogram data={histogram}
                               columnTitles={config.titles}
                               columnFilterElements={[
                                 {...config.filter, fields: TLS_SESSION_FILTER_FIELDS, setFilters: setFilters},
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
