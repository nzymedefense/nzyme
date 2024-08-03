import React, {useContext, useEffect, useState} from 'react'

import GenericWidgetLoadingSpinner from "../../widgets/GenericWidgetLoadingSpinner";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import {TapContext} from "../../../App";
import DNSService from "../../../services/ethernet/DNSService";
import ThreeColumnHistogram from "../../widgets/histograms/ThreeColumnHistogram";

const dnsService = new DNSService()

export default function DNSContactAttemptsTable (props) {

  const timeRange = props.timeRange;

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [data, setData] = useState(null);

  const tapContext = useContext(TapContext);

  const selectedTaps = tapContext.taps;

  useEffect(() => {
    setData(null);
    dnsService.getGlobalPairs(timeRange, selectedTaps, limit, 0, setData);
  }, [selectedTaps, limit, timeRange]);

  if (!data) {
    return <GenericWidgetLoadingSpinner height={300} />
  }

  if (data.total === 0) {
    return (
        <div className="alert alert-info mb-0 mt-2">
          No DNS contact attempts recorded.
        </div>
    )
  }

  return <ThreeColumnHistogram data={data}
                               columnTitles={["Server", "Unique Clients", "Requests"]}
                               customChartColumnValueField="title"
                               limit={limit}
                               setLimit={setLimit} />

}