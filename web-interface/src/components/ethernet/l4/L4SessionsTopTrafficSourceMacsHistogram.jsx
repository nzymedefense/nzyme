import React, {useContext, useEffect, useState} from "react";
import L4Service from "../../../services/ethernet/L4Service";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import {TapContext} from "../../../App";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import {L4_SESSIONS_FILTER_FIELDS} from "./L4SessionFilterFields";
import useSelectedTenant from "../../system/tenantselector/useSelectedTenant";
import ThreeColumnHistogram from "../../widgets/histograms/ThreeColumnHistogram";

const l4Service = new L4Service();

export default function L4SessionsTopTrafficSourceMacsHistogram({filters, setFilters, timeRange, revision}) {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [organizationId, tenantId] = useSelectedTenant();

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [data, setData] = useState(null);

  useEffect(() => {
    setData(null);
    l4Service.getTopTrafficSourceMacs(organizationId, tenantId, selectedTaps, filters, timeRange, limit, 0, setData)
  }, [organizationId, tenantId, selectedTaps, limit, filters, timeRange, revision])

  if (!data) {
    return <GenericWidgetLoadingSpinner height={300} />
  }

  if (data.total === 0) {
    return (
        <div className="alert alert-info mb-0 mt-2">
          No sessions matching filters recorded.
        </div>
    )
  }

  return <ThreeColumnHistogram data={data}
                             columnFilterElements={[
                               {field: "source_mac", fields: L4_SESSIONS_FILTER_FIELDS, setFilters: setFilters},
                               null, null
                             ]}
                             columnTitles={["Source Asset", "RX", "TX"]}
                             limit={limit}
                             setLimit={setLimit} />

}