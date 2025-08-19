import React from "react";
import Flag from "../misc/Flag";

export default function GNSSPRN({constellation, prn}) {

  const flag = () => {
    switch (constellation) {
      case "GPS": return <Flag code="US" />
      case "GLONASS": return <Flag code="RU" />
      case "BeiDou": return <Flag code="CN" />
      case "Galileo": return <Flag code="EU" />
      default: return null;
    }
  }

  return <span>{flag()} {prn}</span>

}