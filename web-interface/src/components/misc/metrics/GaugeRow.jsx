import React from "react";
import numeral from "numeral";

function GaugeRow(props) {

  const title = props.title;
  const gauge = props.gauge;
  const numberFormat = props.numberFormat;

  return (
      <tr>
        <td>{title}</td>
        <td>{numeral(gauge.value).format(numberFormat)}</td>
      </tr>
  )

}

export default GaugeRow;