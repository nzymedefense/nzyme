import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import useSelectedTenant from "../../../system/tenantselector/useSelectedTenant";
import {DEFAULT_LIMIT} from "../../../widgets/LimitSelector";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import ThreeColumnHistogram from "../../../widgets/histograms/ThreeColumnHistogram";
import TimeService from "../../../../services/ethernet/TimeService";
import {NTP_FILTER_FIELDS} from "./NTPFilterFields";

const timeService = new TimeService();

export default function NTPClientRequestResponseRatioHistogram({filters, setFilters, timeRange, revision}) {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [organizationId, tenantId] = useSelectedTenant();

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [data, setData] = useState(null);

  useEffect(() => {
    setData(null);
    timeService.getNTPClientRequestResponseRatioHistogram(organizationId, tenantId, timeRange, filters, selectedTaps, limit, 0, setData);
  }, [organizationId, tenantId, selectedTaps, limit, filters, timeRange, revision])

  if (!data) {
    return <GenericWidgetLoadingSpinner height={300} />
  }

  if (data.total === 0) {
    return (
      <div className="alert alert-info mb-0 mt-2">
        No transactions matching filters recorded.
      </div>
    )
  }

  return <ThreeColumnHistogram data={data}
                               columnFilterElements={[
                                 {field: "client_mac", fields: NTP_FILTER_FIELDS, setFilters: setFilters},
                                 null, null
                               ]}
                               columnTitles={["Source Asset", "Req/Resp Ratio", "Transactions"]}
                               limit={limit}
                               setLimit={setLimit} />

}