import React, {useContext, useEffect, useState} from "react";
import L4Service from "../../../services/ethernet/L4Service";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import {TapContext} from "../../../App";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import TwoColumnHistogram from "../../widgets/histograms/TwoColumnHistogram";
import {L4_SESSIONS_FILTER_FIELDS} from "./L4SessionFilterFields";

const l4Service = new L4Service();

export default function L4SessionsLeastCommonNonEphemeralDestinationPortsHistogram({filters, setFilters, timeRange, revision}) {

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [data, setData] = useState(null);

  useEffect(() => {
    setData(null);
    l4Service.getLeastCommonNonEphemeralDestinationPorts(selectedTaps, filters, timeRange, limit, 0, setData)
  }, [selectedTaps, limit, filters, timeRange, revision])

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

  return <TwoColumnHistogram data={data}
                             columnFilterElements={[
                               {field: "destination_port", fields: L4_SESSIONS_FILTER_FIELDS, setFilters: setFilters},
                               null
                             ]}
                             columnTitles={["Port", "Sessions Count"]}
                             limit={limit}
                             setLimit={setLimit} />

}