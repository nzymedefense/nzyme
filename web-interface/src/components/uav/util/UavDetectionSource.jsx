import {capitalizeFirstLetter} from "../../../util/Tools";

export default function UavDetectionSource(props) {
  const source = props.source;

  if (!source) {
    return "Unknown";
  }

  switch (source) {
    case "REMOTE_ID":
      return "Remote ID";
    default:
      return capitalizeFirstLetter(source.toLowerCase());
  }

}