import React, {useContext, useEffect, useState} from "react";
import TwoColumnHistogram from "../../widgets/histograms/TwoColumnHistogram";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import ThreeColumnHistogram from "../../widgets/histograms/ThreeColumnHistogram";
import {DiscoNetworkContext, MonitoredNetworkContext} from "./DiscoPage";

const dot11Service = new Dot11Service();

function DiscoPairsTable(props) {

  const bssids = props.bssids;
  const highlightValue = props.highlightValue;
  const timeRange = props.timeRange;

  const monitoredNetworkContext = useContext(MonitoredNetworkContext);
  const tapContext = useContext(TapContext);

  const selectedTaps = tapContext.taps;

  const monitoredNetworkId = monitoredNetworkContext && monitoredNetworkContext.network
      ? monitoredNetworkContext.network : null;

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [topPairs, setTopPairs] = useState(null);

  useEffect(() => {
    setTopPairs(null);

    dot11Service.getDiscoTopPairs(
        timeRange, selectedTaps, monitoredNetworkId, bssids, limit, 0, setTopPairs
    );
  }, [selectedTaps, timeRange, limit, monitoredNetworkId]);

  if (!topPairs) {
    return <LoadingSpinner />
  }

  if (topPairs.total === 0) {
    return (
        <div className="alert alert-info mb-0 mt-2">
          No disconnection frames recorded.
        </div>
    )
  }

  return <ThreeColumnHistogram data={topPairs}
                               columnTitles={["Sender", "Receiver", "Frames"]}
                               customChartMarginLeft={235}
                               limit={limit}
                               setLimit={setLimit}
                               highlightValue={highlightValue} />

}

export default DiscoPairsTable;