import React, {useContext, useEffect, useState} from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";
import Dot11Service from "../../../services/Dot11Service";
import {TapContext} from "../../../App";
import SimpleBarChart from "../../charts/SimpleBarChart";

const dot11Service = new Dot11Service();

function DiscoHistogram(props) {

  const discoType = props.discoType;
  const minutes = props.minutes;

  const tapContext = useContext(TapContext);
  const selectedTaps = tapContext.taps;

  const [histogram, setHistogram] = useState(null);

  useEffect(() => {
    dot11Service.getDiscoHistogram(discoType, minutes, selectedTaps, setHistogram);
  }, [discoType, minutes]);

  const formatData = function(data) {
    const result = {}

    Object.keys(data).sort().forEach(function(key) {
      result[key] = data[key]["frame_count"];
    })

    return result
  }

  if (!histogram) {
    return <LoadingSpinner />
  }

  return <SimpleBarChart
      height={200}
      lineWidth={1}
      customMarginBottom={35}
      data={formatData(histogram)}
  />

}

export default DiscoHistogram;