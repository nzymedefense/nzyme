import React, {useContext} from "react";
import {speedInUserUnitSystem, speedUserSymbol} from "../../../util/UserUnitSystem";
import {UserContext} from "../../../App";
import numeral from "numeral";

export default function LocationWind({environment}) {

  const user = useContext(UserContext);

  const describeWind = (speed, gust) => {
    // The descriptor follows whichever is higher. Gusts are usually
    // what determines whether something feels concerning, not the average.
    const effective = Math.max(speed ?? 0, gust ?? 0);

    let label;
    if (effective < 5) return "calm winds";
    else if (effective < 15) label = "light breeze";
    else if (effective < 25) label = "breezy";
    else if (effective < 40) label = "windy";
    else if (effective < 60) label = "strong winds";
    else if (effective < 80) label = "very strong winds";
    else label = "dangerous winds";

    const parts = [];
    if (speed != null) parts.push(`${numeral(speedInUserUnitSystem(speed, user)).format("0")} ${speedUserSymbol(user)}`);
    if (gust != null) parts.push(`gusts ${numeral(speedInUserUnitSystem(gust, user)).format("0")} ${speedUserSymbol(user)}`);

    return `${label} (${parts.join(", ")})`;
  }

  if (!environment || (environment.wind_speed === null && environment.wind_gust === null)) {
    return <span className="text-muted">no wind data</span>
  }

  return <span>{describeWind(environment.wind_speed, environment.wind_gust)}</span>

}