import React from "react";
import LoadingSpinner from "../../misc/LoadingSpinner";

import numeral from "numeral";

function DatabaseSummary(props) {

  const summary = props.summary;

  if (!summary) {
    return <LoadingSpinner />
  }

  return (
      <dl>
        <dt>Total Database Size:</dt>
        <dd>{numeral(summary.total_size).format("0,0b")}</dd>
        <dt>Ethernet Tables Size:</dt>
        <dd>{numeral(summary.ethernet_size).format("0,0b")}</dd>
        <dt>WiFi Tables Size:</dt>
        <dd>{numeral(summary.dot11_size).format("0,0b")}</dd>
      </dl>
  )

}

export default DatabaseSummary;