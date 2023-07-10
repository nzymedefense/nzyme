import React, {useContext, useEffect, useState} from "react";
import {TapContext} from "../../../../App";
import Dot11Service from "../../../../services/Dot11Service";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import SimpleBarChart from "../../../charts/SimpleBarChart";
import numeral from "numeral";

const dot11Service = new Dot11Service();

function SSIDChannelUsageHistogram(props) {

  const bssid = props.bssid;
  const ssid = props.ssid;
  const minutes = props.minutes;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [histogram, setHistogram] = useState(null);

  useEffect(() => {
    dot11Service.getSSIDOfBSSIDActiveChannelHistogram(bssid, ssid, minutes, selectedTaps, setHistogram);
  }, [bssid, ssid, minutes, selectedTaps])

  const formatData = function(data) {
    const x = [];
    const y = [];

    Object.keys(data).forEach(function (i) {
      x.push(data[i]["channel"] + ": " + numeral(data[i]["frequency"]).format("0,0") + " Mhz");
      y.push(data[i]["frames"]);
    });

    return [
      {
        x: x,
        y: y,
        type: 'bar',
        line: { width: 1, shape: 'linear', color: '#2983fe' }
      }
    ]
  }

  if (!histogram) {
    return <LoadingSpinner />
  }

  return (
      <React.Fragment>
        <SimpleBarChart
            height={200}
            customMarginBottom={25}
            finalData={formatData(histogram.channels)} />
      </React.Fragment>
  )


}

export default SSIDChannelUsageHistogram;