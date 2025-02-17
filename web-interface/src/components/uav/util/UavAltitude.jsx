import React from 'react';
import numeral from "numeral";
import {metersToFeet} from "../../../util/Tools";
import UavVerticalAccuracy from "./UavVerticalAccuracy";

export default function UavAltitude(props) {
  const uav = props.uav;

  // We prefer to show height if we have it.
  if (uav.height !== null && uav.height !== undefined) {
    if (uav.height_type === "ABOVE_GROUND") {
      return <span title="Above Ground Level">
        {numeral(metersToFeet(uav.height)).format("0,0")} ft AGL (<UavVerticalAccuracy accuracy={uav.accuracy_vertical} />)
      </span>;
    } else if (uav.height_type === "ABOVE_TAKEOFF_LOCATION") {
      return <span title="Above Takeoff Location">
        {numeral(metersToFeet(uav.height)).format("0,0")} ft ATL (<UavVerticalAccuracy accuracy={uav.accuracy_vertical} />)
      </span>;
    }
  }

  // Pressure altitude is the next most useful info if there is no height available.
  if (uav.altitude_pressure !== null && uav.altitude_pressure !== undefined) {
    return <span title="Pressure Altitude">
      {numeral(metersToFeet(uav.altitude_pressure)).format("0,0")} ft PA (<UavVerticalAccuracy accuracy={uav.accuracy_barometer} />)
    </span>;
  }

  // Geodetic altitude is the fallback.
  if (uav.altitude_geodetic !== null && uav.altitude_geodetic !== undefined) {
    return <span title="Geodetic Altitude">
      {numeral(metersToFeet(uav.altitude_geodetic)).format("0,0")} ft GA (<UavVerticalAccuracy accuracy={uav.accuracy_vertical} />)
    </span>;
  }

  // No altitude at all.
  return "Unknown";

}