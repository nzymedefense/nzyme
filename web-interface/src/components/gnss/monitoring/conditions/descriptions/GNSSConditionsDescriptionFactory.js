import React from "react";

import GNSSConstellationConditionsDescription from "./GNSSConstellationConditionsDescription";
import GNSSFixQualityConditionsDescription from "./GNSSFixQualityConditionsDescription";
import GNSSFixDistanceConditionsDescription from "./GNSSFixDistanceConditionsDescription";
import GNSSPDOPConditionsDescription from "./GNSSPDOPConditionsDescription";
import GNSSClockDriftConditionsDescription from "./GNSSClockDriftConditionsDescription";

const conditionTypeSetToDescription = (type, conditionSet, onConditionRemoved) => {
  switch (type) {
    case "CONSTELLATION":
      return <GNSSConstellationConditionsDescription type={type}
                                                     conditionSet={conditionSet}
                                                     onConditionRemoved={onConditionRemoved} />;
    case "FIX_QUALITY":
      return <GNSSFixQualityConditionsDescription type={type}
                                                  conditionSet={conditionSet}
                                                  onConditionRemoved={onConditionRemoved} />;
    case "FIX_DISTANCE":
      return <GNSSFixDistanceConditionsDescription type={type}
                                                   conditionSet={conditionSet}
                                                   onConditionRemoved={onConditionRemoved} />;
    case "PDOP":
      return <GNSSPDOPConditionsDescription type={type}
                                            conditionSet={conditionSet}
                                            onConditionRemoved={onConditionRemoved} />;
    case "CLOCK_DRIFT":
      return <GNSSClockDriftConditionsDescription type={type}
                                                  conditionSet={conditionSet}
                                                  onConditionRemoved={onConditionRemoved} />;
    default: return JSON.stringify(conditionSet[type])
  }
}

export default conditionTypeSetToDescription;