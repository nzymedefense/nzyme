import React, {useContext} from "react";
import {UserContext} from "../../../App";
import {temperatureInUserUnitSystem, temperatureUserSymbol} from "../../../util/UserUnitSystem";
import numeral from "numeral";

export default function LocationTemperature({environment}) {

  const user = useContext(UserContext);

  if (!environment || environment.temperature === 0) {
    return <span className="text-muted">no temperature data</span>
  }

  return <span>{numeral(temperatureInUserUnitSystem(environment.temperature, user)).format("0")}&deg;{temperatureUserSymbol(user)}</span>

}