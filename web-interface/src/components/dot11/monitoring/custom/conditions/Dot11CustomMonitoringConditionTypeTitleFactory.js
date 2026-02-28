import {capitalizeFirstLetterAndLowercase} from "../../../../../util/Tools";

const conditionTypeToTitle = (type) => {
  switch (type) {
    case "AP_BSSID": return "Access Point BSSID";
    case "FIX_DISTANCE": return "Fix Distance from Tap";
    case "PDOP": return "Dilution of Precision (PDOP)";
    case "CLOCK_DRIFT": return "Clock Drift";
    default: return capitalizeFirstLetterAndLowercase(type)
  }
}

export default conditionTypeToTitle;