import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import Dot11Service from "../../../../services/Dot11Service";
import SimpleLineChart from "../../../widgets/charts/SimpleLineChart";

const dot11Service = new Dot11Service();

function SSIDAdvertisementHistogram(props) {

  const bssid = props.bssid;
  const ssid = props.ssid;
  const parameter = props.parameter;
  const timeRange = props.timeRange;
  const setTimeRange = props.setTimeRange;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [histogram, setHistogram] = useState(null);

  useEffect(() => {
    dot11Service.getSSIDOfBSSIDAdvertisementHistogram(bssid, ssid, timeRange, selectedTaps, setHistogram);
  }, [bssid, ssid, timeRange, selectedTaps])

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key][parameter];
    })

    return result
  }

  if (!histogram) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <SimpleLineChart
          height={200}
          lineWidth={1}
          customMarginBottom={35}
          customMarginRight={20}
          setTimeRange={setTimeRange}
          data={formatData(histogram.values)} />
      </React.Fragment>
  )

}

export default SSIDAdvertisementHistogram;