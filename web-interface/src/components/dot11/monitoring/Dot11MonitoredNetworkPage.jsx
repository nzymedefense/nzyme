import React from "react";
import {useParams} from "react-router-dom";

function Dot11MonitoredNetworkPage() {

  const {uuid} = useParams();

  return (
      <span>foo</span>
  )

}

export default Dot11MonitoredNetworkPage;