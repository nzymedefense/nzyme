import {capitalizeFirstLetter} from "../../../util/Tools";

export default function UavOperatorLocationType(props) {
  const type = props.type;

  if (!type) {
    return "Unknown";
  }

  switch (type) {
    case "TAKEOFF":
      return "Takeoff Location (Static)";
    case "DYNAMIC":
      return "Dynamic/Updating Location";
    case "FIXED":
      return "Fixed Location (Static)";
    case "OTHER":
      return "Other/Unknown";
    default:
      return capitalizeFirstLetter(typetoLowerCase());
  }

}