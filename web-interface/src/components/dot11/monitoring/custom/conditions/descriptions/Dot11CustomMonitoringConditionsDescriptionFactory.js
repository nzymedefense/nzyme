import React from "react";
import Dot11CustomMonitoringAPBSSIDConditionsDescription from "./Dot11CustomMonitoringAPBSSIDConditionsDescription";

const conditionTypeSetToDescription = (type, conditionSet, onConditionRemoved) => {
  switch (type) {
    case "AP_BSSID":
      return <Dot11CustomMonitoringAPBSSIDConditionsDescription type={type}
                                                                conditionSet={conditionSet}
                                                                onConditionRemoved={onConditionRemoved} />;
    default: return JSON.stringify(conditionSet[type])
  }
}

export default conditionTypeSetToDescription;