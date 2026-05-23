import React, {useContext} from "react";
import {UserContext} from "../../../App";
import {distanceInUserUnitSystem, distanceUserSymbol} from "../../../util/UserUnitSystem";
import numeral from "numeral";
export default function LocationVisibility({ environment }) {

  const user = useContext(UserContext);

  const describeVisibility = (meters) => {
    if (meters >= 16000) return "full visibility";

    let label;
    if (meters < 200) label = "dense fog";
    else if (meters < 1000) label = "fog";
    else if (meters < 4000) label = "reduced visibility";
    else if (meters < 10000) label = "hazy";
    else label = "mostly clear";

    return `${label} (${numeral(distanceInUserUnitSystem(meters/1000, user)).format("0,0.0")} ${distanceUserSymbol(user)} visibility)`;
  }

  if (!environment || environment.visibility == null) {
    return <span className="text-muted">no visibility data</span>
  }

  return <span>{describeVisibility(environment.visibility)}</span>

}