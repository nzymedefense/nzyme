import React, {useContext, useEffect, useState} from "react";
import TwoColumnHistogram from "../../widgets/histograms/TwoColumnHistogram";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {DEFAULT_LIMIT} from "../../widgets/LimitSelector";
import {MonitoredNetworkContext} from "./DiscoPage";

const dot11Service = new Dot11Service();

function DiscoSendersTable() {

  const monitoredNetworkContext = useContext(MonitoredNetworkContext);
  const tapContext = useContext(TapContext);

  const selectedTaps = tapContext.taps;

  const [limit, setLimit] = useState(DEFAULT_LIMIT);
  const [topSenders, setTopSenders] = useState(null);

  useEffect(() => {
    setTopSenders(null);
    dot11Service.getDiscoTopSenders(
        24*60, selectedTaps, monitoredNetworkContext.network, limit, 0, setTopSenders
    );
  }, [selectedTaps, limit, monitoredNetworkContext.network]);

  if (!topSenders) {
    return <LoadingSpinner />
  }

  if (topSenders.total === 0) {
    return (
        <div className="alert alert-info mb-0 mt-2">
          No disconnection frames recorded.
        </div>
    )
  }

  return <TwoColumnHistogram data={topSenders}
                             columnTitles={["Sender", "Frames"]}
                             limit={limit}
                             setLimit={setLimit} />

}

export default DiscoSendersTable;