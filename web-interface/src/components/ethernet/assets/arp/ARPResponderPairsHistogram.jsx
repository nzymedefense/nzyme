import React, {useContext, useEffect, useState} from "react";
import AssetsService from "../../../../services/ethernet/AssetsService";
import {DEFAULT_LIMIT} from "../../../widgets/LimitSelector";
import {TapContext} from "../../../../App";
import GenericWidgetLoadingSpinner from "../../../widgets/GenericWidgetLoadingSpinner";
import ThreeColumnHistogram from "../../../widgets/histograms/ThreeColumnHistogram";

const assetsService = new AssetsService();

export default function ARPResponderPairsHistogram(props) {

  const organizationId = props.organizationId;
  const tenantId = props.tenantId;
  const timeRange = props.timeRange;
  const filters = props.filters;
  const revision = props.revision;

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  useEffect(() => {
    setData(null);
    assetsService.getArpResponderPairs(organizationId, tenantId, timeRange, filters, limit, 0, selectedTaps, setData);
  }, [organizationId, tenantId, selectedTaps, limit, filters, timeRange, revision])

  if (!data) {
    return <GenericWidgetLoadingSpinner height={300} />
  }

  if (data.total === 0) {
    return (
        <div className="alert alert-info mb-0 mt-2">
          No ARP requests recorded.
        </div>
    )
  }

  return <ThreeColumnHistogram data={data}
                               columnTitles={["ARP Sender", "ARP Target", "Requests"]}
                               customChartMarginLeft={250}
                               limit={limit}
                               setLimit={setLimit} />

}