import React, {useEffect, useState} from "react";
import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import AssetsService from "../../../services/ethernet/AssetsService";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import ThreeColumnHistogram from "../../widgets/histograms/ThreeColumnHistogram";

const assetsService = new AssetsService();

export default function DisappearedAssetsHistogram({timeRange, filters, organizationId, tenantId, revision}) {

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [histogram, setHistogram] = useState(null);

  useEffect(() => {
    setHistogram(null);
    assetsService.getRecentlyDisappearedAssetsHistogram(organizationId, tenantId, timeRange, filters, limit, 0, setHistogram);
  }, [timeRange, filters, organizationId, tenantId, revision, limit]);

  if (histogram === null) {
    return <GenericWidgetLoadingSpinner height={300}/>
  }

  if (histogram.total === 0) {
    return (
      <div className="alert alert-info mb-0 mt-2">
        No assets found.
      </div>
    )
  }

  return <ThreeColumnHistogram data={histogram}
                               columnTitles={["Asset", "Hostname", "Last Seen"]}
                               customChartMarginLeft={250}
                               limit={limit}
                               setLimit={setLimit} />
}