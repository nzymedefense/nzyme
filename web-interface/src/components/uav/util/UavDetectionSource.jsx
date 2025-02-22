import {capitalizeFirstLetter} from "../../../util/Tools";

export default function UavDetectionSource(props) {
  const source = props.source;

  if (!source) {
    return "Unknown";
  }

  switch (source) {
    case "REMOTE_ID_WIFI":
      return "Remote ID (802.11)";
    case "REMOTE_ID_BLUETOOTH":
      return "Remote ID (Bluetooth)"
    default:
      return capitalizeFirstLetter(source.toLowerCase());
  }

}